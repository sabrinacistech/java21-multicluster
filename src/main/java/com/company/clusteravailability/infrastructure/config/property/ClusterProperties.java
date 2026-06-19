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
        Cache cache
) implements ClusterConfigurationPort {

    public ClusterProperties {
        if (scheduler == null) {
            scheduler = new Scheduler(30, 5);
        }
        if (cache == null) {
            cache = new Cache(60, "cluster_cache");
        }
    }

    public record Scheduler(
            @Positive long defaultPollingIntervalSeconds,
            @Positive long initialDelaySeconds
    ) {
    }

    public record Cache(@Positive long ttlSeconds, @NotBlank String collectionName) {
    }
}
