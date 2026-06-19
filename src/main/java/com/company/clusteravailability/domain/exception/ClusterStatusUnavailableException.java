package com.company.clusteravailability.domain.exception;

public class ClusterStatusUnavailableException extends RuntimeException {

    public ClusterStatusUnavailableException() {
        super("Cluster status is not currently available");
    }
}
