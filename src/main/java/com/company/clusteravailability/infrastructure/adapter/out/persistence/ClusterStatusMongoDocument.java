package com.company.clusteravailability.infrastructure.adapter.out.persistence;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "cluster_status")
public class ClusterStatusMongoDocument {

    @Id
    private String id;

    @Field("cluster_alias")
    private String clusterAlias;

    @Field("active")
    private Boolean active;

    @Field("polling_interval_seconds")
    private Long pollingIntervalSeconds;

    @Field("updated_at")
    private Instant updatedAt;

    @Field("created_at")
    private Instant createdAt;

    public String getId() {
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
