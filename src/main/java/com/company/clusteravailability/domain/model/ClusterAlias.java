package com.company.clusteravailability.domain.model;

import com.company.clusteravailability.domain.exception.InvalidClusterStatusException;

public record ClusterAlias(String value) {

    public ClusterAlias {
        if (value == null || value.isBlank()) {
            throw new InvalidClusterStatusException("Cluster alias must not be blank");
        }
        value = value.trim();
    }
}
