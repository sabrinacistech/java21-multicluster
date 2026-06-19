package com.company.clusteravailability.domain.model;

import com.company.clusteravailability.domain.exception.InvalidClusterStatusException;

public record PollingIntervalSeconds(long value) {

    public PollingIntervalSeconds {
        if (value <= 0) {
            throw new InvalidClusterStatusException("Polling interval seconds must be greater than zero");
        }
    }
}
