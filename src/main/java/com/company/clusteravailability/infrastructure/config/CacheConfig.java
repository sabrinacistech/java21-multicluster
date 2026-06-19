package com.company.clusteravailability.infrastructure.config;

import com.company.clusteravailability.infrastructure.config.property.ClusterProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    CacheManager cacheManager(
            ClusterProperties properties,
            MongoTemplate mongoTemplate,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        return new MongoCacheManager(properties, mongoTemplate, objectMapper, clock);
    }
}
