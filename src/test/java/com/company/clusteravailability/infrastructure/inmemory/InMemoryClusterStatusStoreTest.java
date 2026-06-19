package com.company.clusteravailability.infrastructure.inmemory;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.clusteravailability.domain.model.ClusterAlias;
import com.company.clusteravailability.domain.model.ClusterStatus;
import com.company.clusteravailability.domain.model.PollingIntervalSeconds;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class InMemoryClusterStatusStoreTest {

    @Test
    void updatesAndClearsStatus() {
        InMemoryClusterStatusStore store = new InMemoryClusterStatusStore();
        ClusterStatus status = status(30);

        store.updateStatus(status);
        assertThat(store.getCurrentStatus()).contains(status);

        store.clear();
        assertThat(store.getCurrentStatus()).isEmpty();
    }

    @Test
    void supportsConcurrentUpdates() throws InterruptedException {
        InMemoryClusterStatusStore store = new InMemoryClusterStatusStore();
        CountDownLatch latch = new CountDownLatch(50);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= 50; i++) {
                long interval = i;
                executor.submit(() -> {
                    store.updateStatus(status(interval));
                    latch.countDown();
                });
            }
        }

        latch.await();
        assertThat(store.getCurrentStatus()).isPresent();
    }

    private static ClusterStatus status(long interval) {
        return new ClusterStatus(
                new ClusterAlias("primary-cluster"),
                true,
                new PollingIntervalSeconds(interval),
                Instant.parse("2026-06-19T10:15:00Z")
        );
    }
}
