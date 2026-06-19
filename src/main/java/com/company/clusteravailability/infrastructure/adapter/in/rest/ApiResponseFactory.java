package com.company.clusteravailability.infrastructure.adapter.in.rest;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ApiResponseFactory {

    private final Clock clock;
    private final String serviceName;

    ApiResponseFactory(Clock clock, @Value("${spring.application.name}") String serviceName) {
        this.clock = clock;
        this.serviceName = serviceName;
    }

    <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(metadata(), data, List.of());
    }

    <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(metadata(), null, List.of(new ApiError(code, message)));
    }

    private ApiMetadata metadata() {
        return new ApiMetadata(Instant.now(clock), serviceName, "1.0.0");
    }
}
