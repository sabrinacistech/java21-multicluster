package com.company.clusteravailability.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClusterStatusJpaRepository extends JpaRepository<ClusterStatusJpaEntity, Long> {

    Optional<ClusterStatusJpaEntity> findFirstByClusterAlias(String clusterAlias);

    @Query("""
            select c.pollingIntervalSeconds as pollingIntervalSeconds, c.updatedAt as updatedAt
            from ClusterStatusJpaEntity c
            where c.clusterAlias = :clusterAlias
            """)
    Optional<ClusterStatusProjection> findPollingConfigByClusterAlias(@Param("clusterAlias") String clusterAlias);
}
