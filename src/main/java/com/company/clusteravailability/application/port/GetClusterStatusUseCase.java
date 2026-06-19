package com.company.clusteravailability.application.port;

import com.company.clusteravailability.domain.model.ClusterStatus;

public interface GetClusterStatusUseCase {

    ClusterStatus getCurrentStatus();
}
