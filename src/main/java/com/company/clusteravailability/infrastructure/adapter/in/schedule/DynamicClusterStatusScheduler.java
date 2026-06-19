package com.company.clusteravailability.infrastructure.adapter.in.schedule;

import com.company.clusteravailability.application.port.SynchronizeClusterStatusUseCase;
import com.company.clusteravailability.domain.model.ClusterAlias;
import com.company.clusteravailability.domain.port.ClusterStatusRepositoryPort;
import com.company.clusteravailability.infrastructure.config.property.ClusterProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.stereotype.Component;

@Component
public class DynamicClusterStatusScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicClusterStatusScheduler.class);

    private final TaskScheduler taskScheduler;
    private final SynchronizeClusterStatusUseCase synchronizeUseCase;
    private final ClusterStatusRepositoryPort repository;
    private final ClusterProperties properties;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong currentIntervalSeconds;
    private ScheduledFuture<?> scheduledFuture;

    public DynamicClusterStatusScheduler(
            TaskScheduler taskScheduler,
            SynchronizeClusterStatusUseCase synchronizeUseCase,
            ClusterStatusRepositoryPort repository,
            ClusterProperties properties
    ) {
        this.taskScheduler = taskScheduler;
        this.synchronizeUseCase = synchronizeUseCase;
        this.repository = repository;
        this.properties = properties;
        this.currentIntervalSeconds = new AtomicLong(properties.scheduler().defaultPollingIntervalSeconds());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        runSafely();
        Trigger trigger = context -> {
            Instant lastCompletion = context.lastCompletion();
            Instant base = lastCompletion == null
                    ? Instant.now().plusSeconds(properties.scheduler().initialDelaySeconds())
                    : lastCompletion;
            return base.plus(Duration.ofSeconds(currentIntervalSeconds.get()));
        };
        scheduledFuture = taskScheduler.schedule(this::runSafely, trigger);
    }

    void runSafely() {
        if (!running.compareAndSet(false, true)) {
            LOGGER.warn("cluster_status_sync_skipped result=already_running");
            return;
        }
        Instant startedAt = Instant.now();
        try {
            synchronizeUseCase.synchronize();
            refreshPollingInterval();
        } finally {
            running.set(false);
            long durationMs = Duration.between(startedAt, Instant.now()).toMillis();
            LOGGER.info("cluster_status_scheduler_cycle_finished durationMs={} intervalSeconds={}", durationMs, currentIntervalSeconds.get());
        }
    }

    void refreshPollingInterval() {
        long previous = currentIntervalSeconds.get();
        long next = repository.findPollingConfigByAlias(new ClusterAlias(properties.alias()))
                .map(config -> config.pollingIntervalSeconds().value())
                .orElse(properties.scheduler().defaultPollingIntervalSeconds());
        currentIntervalSeconds.set(next);
        if (previous != next) {
            LOGGER.info("cluster_status_polling_interval_changed previousSeconds={} nextSeconds={}", previous, next);
        }
    }

    public long currentIntervalSeconds() {
        return currentIntervalSeconds.get();
    }

    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }
}
