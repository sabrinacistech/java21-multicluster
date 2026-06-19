package com.company.clusteravailability.domain.exception;

public class ClusterStatusNotFoundException extends RuntimeException {

    public ClusterStatusNotFoundException(String alias) {
        super("Cluster status not found for alias: " + alias);
    }
}
