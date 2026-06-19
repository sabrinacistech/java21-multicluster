package com.company.clusteravailability.domain.port;

import com.company.clusteravailability.domain.model.ClusterStatus;
import java.util.Optional;

public interface ClusterStatusStorePort {

    Optional<ClusterStatus> getCurrentStatus();

    void updateStatus(ClusterStatus status);

    void clear();
}
