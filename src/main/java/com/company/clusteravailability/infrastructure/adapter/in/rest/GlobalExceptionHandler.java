package com.company.clusteravailability.infrastructure.adapter.in.rest;

import com.company.clusteravailability.domain.exception.ClusterStatusNotFoundException;
import com.company.clusteravailability.domain.exception.ClusterStatusRepositoryException;
import com.company.clusteravailability.domain.exception.ClusterStatusUnavailableException;
import com.company.clusteravailability.domain.exception.InvalidClusterStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ApiResponseFactory responseFactory;

    public GlobalExceptionHandler(ApiResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @ExceptionHandler(ClusterStatusNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleNotFound(ClusterStatusNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "CLUSTER_STATUS_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ClusterStatusUnavailableException.class)
    ResponseEntity<ApiResponse<Void>> handleUnavailable(ClusterStatusUnavailableException ex) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "CLUSTER_STATUS_UNAVAILABLE", ex.getMessage());
    }

    @ExceptionHandler(ClusterStatusRepositoryException.class)
    ResponseEntity<ApiResponse<Void>> handleRepository(ClusterStatusRepositoryException ex) {
        LOGGER.error("cluster_status_repository_error", ex);
        return error(HttpStatus.BAD_GATEWAY, "CLUSTER_STATUS_REPOSITORY_ERROR", "Cluster status persistence is unavailable");
    }

    @ExceptionHandler(InvalidClusterStatusException.class)
    ResponseEntity<ApiResponse<Void>> handleInvalidStatus(InvalidClusterStatusException ex) {
        LOGGER.error("cluster_status_invalid_error", ex);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "INVALID_CLUSTER_STATUS", "Cluster status data is invalid");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        LOGGER.error("cluster_status_unexpected_error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Unexpected server error");
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(responseFactory.error(code, message));
    }
}
