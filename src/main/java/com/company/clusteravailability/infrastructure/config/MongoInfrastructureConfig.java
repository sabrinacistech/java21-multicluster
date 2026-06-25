package com.company.clusteravailability.infrastructure.config;

import com.company.clusteravailability.infrastructure.config.property.ClusterProperties;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
public class MongoInfrastructureConfig {

    private final MongoTemplate mongoTemplate;
    private final ClusterProperties properties;

    public MongoInfrastructureConfig(MongoTemplate mongoTemplate, ClusterProperties properties) {
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
    }

    @Bean
    MongoClientSettingsBuilderCustomizer clusterMongoClientSettingsCustomizer() {
        ClusterProperties.Mongo mongo = properties.mongo();
        return builder -> builder
                .applyToClusterSettings(settings -> settings.serverSelectionTimeout(mongo.serverSelectionTimeoutMs(), TimeUnit.MILLISECONDS))
                .applyToSocketSettings(settings -> settings
                        .connectTimeout(mongo.connectTimeoutMs(), TimeUnit.MILLISECONDS)
                        .readTimeout(mongo.readTimeoutMs(), TimeUnit.MILLISECONDS))
                .applyToConnectionPoolSettings(settings -> settings
                        .maxSize(mongo.maxConnectionPoolSize())
                        .minSize(mongo.minConnectionPoolSize())
                        .maxWaitTime(mongo.maxWaitTimeMs(), TimeUnit.MILLISECONDS));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureMongoCacheIndexes() {
        String collectionName = properties.cache().collectionName();
        mongoTemplate.indexOps(collectionName).ensureIndex(
                new Index()
                        .on("expires_at", Sort.Direction.ASC)
                        .named("idx_cluster_cache_expires_at")
                        .expire(0, TimeUnit.SECONDS)
        );
        mongoTemplate.indexOps(collectionName).ensureIndex(
                new Index()
                        .on("cache_name", Sort.Direction.ASC)
                        .on("cache_key", Sort.Direction.ASC)
                        .named("idx_cluster_cache_name_key")
        );
    }
}
