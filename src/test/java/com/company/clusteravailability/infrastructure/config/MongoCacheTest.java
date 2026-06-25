package com.company.clusteravailability.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

class MongoCacheTest {

    @Test
    void returnsCacheMissWhenMongoLookupFails() {
        MongoTemplate mongoTemplate = org.mockito.Mockito.mock(MongoTemplate.class);
        when(mongoTemplate.findOne(any(Query.class), eq(MongoCacheEntryDocument.class), eq("cluster_cache")))
                .thenThrow(new RuntimeException("mongo down"));
        MongoCache cache = cache(mongoTemplate);

        assertThat(cache.get("primary-cluster")).isNull();
    }

    @Test
    void doesNotThrowWhenMongoPutFails() {
        MongoTemplate mongoTemplate = org.mockito.Mockito.mock(MongoTemplate.class);
        when(mongoTemplate.upsert(any(Query.class), any(Update.class), eq("cluster_cache")))
                .thenThrow(new RuntimeException("mongo down"));
        MongoCache cache = cache(mongoTemplate);

        assertThatCode(() -> cache.put("primary-cluster", "active"))
                .doesNotThrowAnyException();
    }

    private static MongoCache cache(MongoTemplate mongoTemplate) {
        return new MongoCache(
                "clusterStatusByAlias",
                "cluster_cache",
                mongoTemplate,
                new ObjectMapper(),
                Clock.systemUTC(),
                Duration.ofSeconds(60),
                CircuitBreakerRegistry.ofDefaults().circuitBreaker("mongoCache")
        );
    }
}
