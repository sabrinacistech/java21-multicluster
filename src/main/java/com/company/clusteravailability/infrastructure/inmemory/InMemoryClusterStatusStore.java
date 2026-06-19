package com.company.clusteravailability.infrastructure.inmemory;

import com.company.clusteravailability.domain.model.ClusterStatus;
import com.company.clusteravailability.domain.port.ClusterStatusStorePort;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class InMemoryClusterStatusStore implements ClusterStatusStorePort {

    private final AtomicReference<ClusterStatus> currentStatus = new AtomicReference<>();

    @Override
    public Optional<ClusterStatus> getCurrentStatus() {
        return Optional.ofNullable(currentStatus.get());
    }

    @Override
    public void updateStatus(ClusterStatus status) {
        currentStatus.set(status);
    }

    @Override
    public void clear() {
        currentStatus.set(null);
    }
}
