package com.company.clusteravailability.domain.model;

import com.company.clusteravailability.domain.exception.InvalidClusterStatusException;
import java.time.Instant;

public record ClusterStatus(
        ClusterAlias alias,
        Boolean active,
        PollingIntervalSeconds pollingIntervalSeconds,
        Instant updatedAt
) {

    public ClusterStatus {
        if (alias == null) {
            throw new InvalidClusterStatusException("Cluster alias must not be null");
        }
        if (active == null) {
            throw new InvalidClusterStatusException("Cluster active flag must not be null");
        }
        if (pollingIntervalSeconds == null) {
            throw new InvalidClusterStatusException("Polling interval must not be null");
        }
        if (updatedAt == null) {
            throw new InvalidClusterStatusException("Updated timestamp must not be null");
        }
    }
}
