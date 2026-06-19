package com.company.clusteravailability.infrastructure.adapter.in.rest;

import com.company.clusteravailability.domain.model.ClusterStatus;
import org.springframework.stereotype.Component;

@Component
class ClusterStatusRestMapper {

    ClusterStatusResponse toResponse(ClusterStatus status) {
        return new ClusterStatusResponse(
                status.alias().value(),
                status.active(),
                status.pollingIntervalSeconds().value(),
                status.updatedAt()
        );
    }
}
