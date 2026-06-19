package com.company.clusteravailability.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.company.clusteravailability.domain.exception.ClusterStatusUnavailableException;
import com.company.clusteravailability.domain.model.ClusterAlias;
import com.company.clusteravailability.domain.model.ClusterStatus;
import com.company.clusteravailability.domain.model.PollingIntervalSeconds;
import com.company.clusteravailability.domain.port.ClusterStatusStorePort;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetClusterStatusServiceTest {

    @Mock
    private ClusterStatusStorePort store;

    @Test
    void returnsCurrentStatusWhenAvailable() {
        ClusterStatus status = status();
        when(store.getCurrentStatus()).thenReturn(Optional.of(status));

        assertThat(new GetClusterStatusService(store).getCurrentStatus()).isEqualTo(status);
    }

    @Test
    void throwsUnavailableWhenStatusIsMissing() {
        when(store.getCurrentStatus()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new GetClusterStatusService(store).getCurrentStatus())
                .isInstanceOf(ClusterStatusUnavailableException.class);
    }

    private static ClusterStatus status() {
        return new ClusterStatus(new ClusterAlias("primary-cluster"), true, new PollingIntervalSeconds(30), Instant.now());
    }
}
