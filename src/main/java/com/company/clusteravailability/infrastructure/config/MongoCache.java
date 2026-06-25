package com.company.clusteravailability.infrastructure.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

class MongoCache implements Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCache.class);

    private final String name;
    private final String collectionName;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Duration ttl;
    private final CircuitBreaker circuitBreaker;

    MongoCache(
            String name,
            String collectionName,
            MongoTemplate mongoTemplate,
            ObjectMapper objectMapper,
            Clock clock,
            Duration ttl,
            CircuitBreaker circuitBreaker
    ) {
        this.name = name;
        this.collectionName = collectionName;
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.ttl = ttl;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return mongoTemplate;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object value = lookup(key);
        return value == null ? null : new SimpleValueWrapper(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        Object value = lookup(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            LOGGER.warn("mongo_cache_type_mismatch cacheName={} expectedType={} actualType={}", name, type.getName(), value.getClass().getName());
            evict(key);
            return null;
        }
        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = lookup(key);
        if (value != null) {
            return (T) value;
        }
        try {
            T loaded = valueLoader.call();
            put(key, loaded);
            return loaded;
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    @Override
    public void put(Object key, Object value) {
        if (value == null) {
            evict(key);
            return;
        }
        String cacheKey = key.toString();
        String id = id(cacheKey);
        try {
            String payload = objectMapper.writeValueAsString(value);
            circuitBreaker.executeRunnable(() -> {
                Query query = Query.query(Criteria.where("_id").is(id));
                Update update = new Update()
                        .set("cache_name", name)
                        .set("cache_key", cacheKey)
                        .set("payload", payload)
                        .set("type", value.getClass().getName())
                        .set("expires_at", Instant.now(clock).plus(ttl));
                mongoTemplate.upsert(query, update, collectionName);
            });
        } catch (JsonProcessingException ex) {
            LOGGER.warn("mongo_cache_serialize_failed cacheName={} key={} errorCode={}", name, cacheKey, ex.getClass().getSimpleName());
        } catch (RuntimeException ex) {
            LOGGER.warn("mongo_cache_put_failed cacheName={} key={} errorCode={}", name, cacheKey, ex.getClass().getSimpleName());
        }
    }

    @Override
    public void evict(Object key) {
        String cacheKey = key.toString();
        try {
            circuitBreaker.executeRunnable(() -> mongoTemplate.remove(Query.query(Criteria.where("_id").is(id(cacheKey))), collectionName));
        } catch (RuntimeException ex) {
            LOGGER.warn("mongo_cache_evict_failed cacheName={} key={} errorCode={}", name, cacheKey, ex.getClass().getSimpleName());
        }
    }

    @Override
    public void clear() {
        try {
            circuitBreaker.executeRunnable(() -> mongoTemplate.remove(Query.query(Criteria.where("cache_name").is(name)), collectionName));
        } catch (RuntimeException ex) {
            LOGGER.warn("mongo_cache_clear_failed cacheName={} errorCode={}", name, ex.getClass().getSimpleName());
        }
    }

    private Object lookup(Object key) {
        try {
            return circuitBreaker.executeSupplier(() -> lookupInternal(key));
        } catch (RuntimeException ex) {
            LOGGER.warn("mongo_cache_lookup_failed cacheName={} key={} errorCode={}", name, key, ex.getClass().getSimpleName());
            return null;
        }
    }

    private Object lookupInternal(Object key) {
        Query query = Query.query(Criteria.where("_id").is(id(key.toString())));
        MongoCacheEntryDocument entry = mongoTemplate.findOne(query, MongoCacheEntryDocument.class, collectionName);
        if (entry == null) {
            return null;
        }
        if (entry.getExpiresAt().isBefore(Instant.now(clock))) {
            evict(key);
            return null;
        }
        try {
            Class<?> type = Class.forName(entry.getType());
            return objectMapper.readValue(entry.getPayload(), type);
        } catch (ClassNotFoundException | JsonProcessingException ex) {
            evict(key);
            LOGGER.warn("mongo_cache_deserialize_failed cacheName={} key={} errorCode={}", name, key, ex.getClass().getSimpleName());
            return null;
        }
    }

    private String id(String key) {
        return name + "::" + key;
    }
}
