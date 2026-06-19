package com.company.clusteravailability.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Standard API response")
public record ApiResponse<T>(
        ApiMetadata metadata,
        T data,
        List<ApiError> errors
) {
}
