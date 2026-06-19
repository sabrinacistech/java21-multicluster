package com.company.clusteravailability.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Standard API metadata")
public record ApiMetadata(
        Instant timestamp,
        @Schema(example = "cluster-availability-service") String service,
        @Schema(example = "1.0.0") String version
) {
}
