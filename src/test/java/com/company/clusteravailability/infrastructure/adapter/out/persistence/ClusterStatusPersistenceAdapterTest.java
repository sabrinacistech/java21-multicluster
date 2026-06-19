package com.company.clusteravailability.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.clusteravailability.domain.model.ClusterAlias;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ClusterStatusPersistenceAdapterTest {

    @Test
    void mapsEntityToDomain() {
        ClusterStatusJpaRepository repository = mock(ClusterStatusJpaRepository.class);
        ClusterStatusJpaEntity entity = mock(ClusterStatusJpaEntity.class);
        when(entity.getClusterAlias()).thenReturn("primary-cluster");
        when(entity.getActive()).thenReturn(true);
        when(entity.getPollingIntervalSeconds()).thenReturn(30L);
        when(entity.getUpdatedAt()).thenReturn(Instant.parse("2026-06-19T10:15:00Z"));
        when(repository.findFirstByClusterAlias("primary-cluster")).thenReturn(Optional.of(entity));

        ClusterStatusPersistenceAdapter adapter = new ClusterStatusPersistenceAdapter(repository, new ClusterStatusMapper());

        assertThat(adapter.findByAlias(new ClusterAlias("primary-cluster")))
                .isPresent()
                .get()
                .extracting(status -> status.pollingIntervalSeconds().value())
                .isEqualTo(30L);
    }
}
