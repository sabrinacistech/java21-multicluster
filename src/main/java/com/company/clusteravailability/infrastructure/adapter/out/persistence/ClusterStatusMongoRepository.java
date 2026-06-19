package com.company.clusteravailability.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ClusterStatusMongoRepository extends MongoRepository<ClusterStatusMongoDocument, String> {

    Optional<ClusterStatusMongoDocument> findFirstByClusterAlias(String clusterAlias);

    @Query(value = "{ 'cluster_alias': ?0 }", fields = "{ 'polling_interval_seconds': 1, 'updated_at': 1 }")
    Optional<ClusterStatusMongoDocument> findPollingConfigByClusterAlias(String clusterAlias);
}
