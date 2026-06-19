package com.company.clusteravailability.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "cluster_status", schema = "dbo")
public class ClusterStatusJpaEntity {

    @Id
    private Long id;

    @Column(name = "cluster_alias", nullable = false)
    private String clusterAlias;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "polling_interval_seconds", nullable = false)
    private Long pollingIntervalSeconds;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public String getClusterAlias() {
        return clusterAlias;
    }

    public Boolean getActive() {
        return active;
    }

    public Long getPollingIntervalSeconds() {
        return pollingIntervalSeconds;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
