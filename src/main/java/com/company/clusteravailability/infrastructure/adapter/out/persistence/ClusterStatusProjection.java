package com.company.clusteravailability.infrastructure.adapter.out.persistence;

import java.time.Instant;

public interface ClusterStatusProjection {

    Long getPollingIntervalSeconds();

    Instant getUpdatedAt();
}
