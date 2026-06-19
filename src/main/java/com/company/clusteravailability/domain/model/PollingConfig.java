package com.company.clusteravailability.domain.model;

import java.time.Instant;

public record PollingConfig(PollingIntervalSeconds pollingIntervalSeconds, Instant updatedAt) {
}
