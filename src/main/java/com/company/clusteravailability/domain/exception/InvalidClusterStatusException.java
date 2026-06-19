package com.company.clusteravailability.domain.exception;

public class InvalidClusterStatusException extends RuntimeException {

    public InvalidClusterStatusException(String message) {
        super(message);
    }
}
