package com.company.clusteravailability.infrastructure.config.property;

import com.company.clusteravailability.application.port.ClusterConfigurationPort;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cluster")
public record ClusterProperties(
        @NotBlank String alias,
        Scheduler scheduler,
        Cache cache,
        Mongo mongo
) implements ClusterConfigurationPort {

    public ClusterProperties {
        if (scheduler == null) {
            scheduler = new Scheduler(30, 5);
        }
        if (cache == null) {
            cache = new Cache(60, "cluster_cache");
        }
        if (mongo == null) {
            mongo = new Mongo(500, 1_000, 1_000, 100, 0, 500);
        }
    }

    public record Scheduler(
            @Positive long defaultPollingIntervalSeconds,
            @Positive long initialDelaySeconds
    ) {
    }

    public record Cache(@Positive long ttlSeconds, @NotBlank String collectionName) {
    }

    public record Mongo(
            @Positive int connectTimeoutMs,
            @Positive int readTimeoutMs,
            @Positive int serverSelectionTimeoutMs,
            @Positive int maxConnectionPoolSize,
            int minConnectionPoolSize,
            @Positive int maxWaitTimeMs
    ) {
    }
}
