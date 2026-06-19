package com.company.clusteravailability.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.clusteravailability.domain.exception.InvalidClusterStatusException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ClusterStatusTest {

    @Test
    void createsValidStatus() {
        ClusterStatus status = new ClusterStatus(
                new ClusterAlias("primary-cluster"),
                true,
                new PollingIntervalSeconds(30),
                Instant.parse("2026-06-19T10:15:00Z")
        );

        assertThat(status.alias().value()).isEqualTo("primary-cluster");
        assertThat(status.active()).isTrue();
    }

    @Test
    void rejectsInvalidData() {
        assertThatThrownBy(() -> new ClusterAlias(" "))
                .isInstanceOf(InvalidClusterStatusException.class);
        assertThatThrownBy(() -> new PollingIntervalSeconds(0))
                .isInstanceOf(InvalidClusterStatusException.class);
        assertThatThrownBy(() -> new ClusterStatus(new ClusterAlias("a"), null, new PollingIntervalSeconds(1), Instant.now()))
                .isInstanceOf(InvalidClusterStatusException.class);
    }
}
