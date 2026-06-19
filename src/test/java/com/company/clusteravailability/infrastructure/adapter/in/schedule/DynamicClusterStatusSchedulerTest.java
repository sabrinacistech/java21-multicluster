package com.company.clusteravailability.infrastructure.adapter.in.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.clusteravailability.application.port.SynchronizeClusterStatusUseCase;
import com.company.clusteravailability.domain.model.ClusterAlias;
import com.company.clusteravailability.domain.model.PollingConfig;
import com.company.clusteravailability.domain.model.PollingIntervalSeconds;
import com.company.clusteravailability.domain.port.ClusterStatusRepositoryPort;
import com.company.clusteravailability.infrastructure.config.property.ClusterProperties;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class DynamicClusterStatusSchedulerTest {

    @Test
    void refreshesIntervalFromPersistenceConfig() {
        ClusterStatusRepositoryPort repository = mock(ClusterStatusRepositoryPort.class);
        when(repository.findPollingConfigByAlias(new ClusterAlias("primary-cluster")))
                .thenReturn(Optional.of(new PollingConfig(new PollingIntervalSeconds(45), Instant.now())));

        DynamicClusterStatusScheduler scheduler = new DynamicClusterStatusScheduler(
                new ThreadPoolTaskScheduler(),
                mock(SynchronizeClusterStatusUseCase.class),
                repository,
                new ClusterProperties("primary-cluster", new ClusterProperties.Scheduler(30, 5), new ClusterProperties.Cache(60))
        );

        scheduler.refreshPollingInterval();

        assertThat(scheduler.currentIntervalSeconds()).isEqualTo(45);
    }
}
