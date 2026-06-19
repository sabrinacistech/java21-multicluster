package com.company.clusteravailability.application.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.company.clusteravailability.application.port.ClusterConfigurationPort;
import com.company.clusteravailability.domain.exception.ClusterStatusRepositoryException;
import com.company.clusteravailability.domain.model.ClusterAlias;
import com.company.clusteravailability.domain.model.ClusterStatus;
import com.company.clusteravailability.domain.model.PollingIntervalSeconds;
import com.company.clusteravailability.domain.port.ClusterStatusRepositoryPort;
import com.company.clusteravailability.domain.port.ClusterStatusStorePort;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SynchronizeClusterStatusServiceTest {

    @Mock
    private ClusterStatusRepositoryPort repository;
    @Mock
    private ClusterStatusStorePort store;

    private final ClusterConfigurationPort configuration = () -> "primary-cluster";

    @Test
    void updatesStoreWhenStatusExists() {
        ClusterStatus status = status();
        when(repository.findByAlias(new ClusterAlias("primary-cluster"))).thenReturn(Optional.of(status));

        new SynchronizeClusterStatusService(repository, store, configuration).synchronize();

        verify(store).updateStatus(status);
    }

    @Test
    void clearsStoreWhenAliasDoesNotExist() {
        when(repository.findByAlias(new ClusterAlias("primary-cluster"))).thenReturn(Optional.empty());

        new SynchronizeClusterStatusService(repository, store, configuration).synchronize();

        verify(store).clear();
    }

    @Test
    void keepsPreviousStatusWhenRepositoryFails() {
        when(repository.findByAlias(new ClusterAlias("primary-cluster")))
                .thenThrow(new ClusterStatusRepositoryException("boom", new RuntimeException("mongo")));

        new SynchronizeClusterStatusService(repository, store, configuration).synchronize();

        verifyNoInteractions(store);
    }

    private static ClusterStatus status() {
        return new ClusterStatus(new ClusterAlias("primary-cluster"), true, new PollingIntervalSeconds(30), Instant.now());
    }
}
