package com.company.clusteravailability.domain.port;

import com.company.clusteravailability.domain.model.ClusterAlias;
import com.company.clusteravailability.domain.model.ClusterStatus;
import com.company.clusteravailability.domain.model.PollingConfig;
import java.util.Optional;

public interface ClusterStatusRepositoryPort {

    Optional<ClusterStatus> findByAlias(ClusterAlias alias);

    Optional<PollingConfig> findPollingConfigByAlias(ClusterAlias alias);
}
