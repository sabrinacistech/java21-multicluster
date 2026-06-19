package com.company.clusteravailability.infrastructure.adapter.out.persistence;

import com.company.clusteravailability.domain.model.ClusterAlias;
import com.company.clusteravailability.domain.model.ClusterStatus;
import com.company.clusteravailability.domain.model.PollingConfig;
import com.company.clusteravailability.domain.model.PollingIntervalSeconds;
import org.springframework.stereotype.Component;

@Component
public class ClusterStatusMapper {

    ClusterStatus toDomain(ClusterStatusJpaEntity entity) {
        return new ClusterStatus(
                new ClusterAlias(entity.getClusterAlias()),
                entity.getActive(),
                new PollingIntervalSeconds(entity.getPollingIntervalSeconds()),
                entity.getUpdatedAt()
        );
    }

    PollingConfig toDomain(ClusterStatusProjection projection) {
        return new PollingConfig(
                new PollingIntervalSeconds(projection.getPollingIntervalSeconds()),
                projection.getUpdatedAt()
        );
    }
}
