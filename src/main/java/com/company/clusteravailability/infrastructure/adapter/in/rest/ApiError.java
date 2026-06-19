package com.company.clusteravailability.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API error item")
public record ApiError(
        @Schema(example = "CLUSTER_STATUS_UNAVAILABLE") String code,
        @Schema(example = "Cluster status is not currently available") String message
) {
}
