package com.company.clusteravailability.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Current cluster status payload")
public record ClusterStatusResponse(
        @Schema(example = "primary-cluster") String alias,
        @Schema(example = "true") boolean active,
        @Schema(example = "30") long pollingIntervalSeconds,
        Instant updatedAt
) {
}
