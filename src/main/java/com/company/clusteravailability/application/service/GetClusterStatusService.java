package com.company.clusteravailability.application.service;

import com.company.clusteravailability.application.port.GetClusterStatusUseCase;
import com.company.clusteravailability.domain.exception.ClusterStatusUnavailableException;
import com.company.clusteravailability.domain.model.ClusterStatus;
import com.company.clusteravailability.domain.port.ClusterStatusStorePort;
import org.springframework.stereotype.Service;

@Service
public class GetClusterStatusService implements GetClusterStatusUseCase {

    private final ClusterStatusStorePort store;

    public GetClusterStatusService(ClusterStatusStorePort store) {
        this.store = store;
    }

    @Override
    public ClusterStatus getCurrentStatus() {
        return store.getCurrentStatus().orElseThrow(ClusterStatusUnavailableException::new);
    }
}
