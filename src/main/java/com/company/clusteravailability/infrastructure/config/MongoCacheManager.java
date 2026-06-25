package com.company.clusteravailability.infrastructure.config;

import com.company.clusteravailability.infrastructure.config.property.ClusterProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;

public class MongoCacheManager implements CacheManager {

    private final Map<String, Cache> caches = new ConcurrentHashMap<>();
    private final String collectionName;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Duration ttl;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public MongoCacheManager(
            ClusterProperties properties,
            MongoTemplate mongoTemplate,
            ObjectMapper objectMapper,
            Clock clock,
            CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        this.collectionName = properties.cache().collectionName();
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.ttl = Duration.ofSeconds(properties.cache().ttlSeconds());
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, cacheName -> new MongoCache(
                cacheName,
                collectionName,
                mongoTemplate,
                objectMapper,
                clock,
                ttl,
                circuitBreakerRegistry.circuitBreaker("mongoCache")
        ));
    }

    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }
}
