package com.company.clusteravailability.application.service;

import com.company.clusteravailability.application.port.SynchronizeClusterStatusUseCase;
import com.company.clusteravailability.application.port.ClusterConfigurationPort;
import com.company.clusteravailability.domain.exception.InvalidClusterStatusException;
import com.company.clusteravailability.domain.model.ClusterAlias;
import com.company.clusteravailability.domain.model.ClusterStatus;
import com.company.clusteravailability.domain.port.ClusterStatusRepositoryPort;
import com.company.clusteravailability.domain.port.ClusterStatusStorePort;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SynchronizeClusterStatusService implements SynchronizeClusterStatusUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizeClusterStatusService.class);

    private final ClusterStatusRepositoryPort repository;
    private final ClusterStatusStorePort store;
    private final ClusterConfigurationPort configuration;

    public SynchronizeClusterStatusService(
            ClusterStatusRepositoryPort repository,
            ClusterStatusStorePort store,
            ClusterConfigurationPort configuration
    ) {
        this.repository = repository;
        this.store = store;
        this.configuration = configuration;
    }

    @Override
    public void synchronize() {
        Instant startedAt = Instant.now();
        String safeAlias = LogSanitizer.sanitize(configuration.alias());
        LOGGER.info("cluster_status_sync_started {}={}", LogKey.CLUSTER_ALIAS, safeAlias);
        try {
            Optional<ClusterStatus> status = repository.findByAlias(new ClusterAlias(configuration.alias()));
            if (status.isEmpty()) {
                store.clear();
                LOGGER.warn("cluster_status_sync_not_found {}={}", LogKey.CLUSTER_ALIAS, safeAlias);
                return;
            }
            store.updateStatus(status.get());
            long durationMs = Duration.between(startedAt, Instant.now()).toMillis();
            LOGGER.info(
                    "cluster_status_sync_finished {}={} {}={} {}={} {}={}",
                    LogKey.CLUSTER_ALIAS,
                    safeAlias,
                    LogKey.POLLING_INTERVAL_SECONDS,
                    status.get().pollingIntervalSeconds().value(),
                    LogKey.SYNC_DURATION_MS,
                    durationMs,
                    LogKey.RESULT,
                    "success"
            );
        } catch (InvalidClusterStatusException ex) {
            store.clear();
            LOGGER.error(
                    "cluster_status_sync_invalid_data {}={} {}={}",
                    LogKey.CLUSTER_ALIAS,
                    safeAlias,
                    LogKey.ERROR_CODE,
                    ex.getClass().getSimpleName(),
                    ex
            );
        } catch (RuntimeException ex) {
            LOGGER.error(
                    "cluster_status_sync_failed {}={} {}={}",
                    LogKey.CLUSTER_ALIAS,
                    safeAlias,
                    LogKey.ERROR_CODE,
                    ex.getClass().getSimpleName(),
                    ex
            );
        }
    }
}
