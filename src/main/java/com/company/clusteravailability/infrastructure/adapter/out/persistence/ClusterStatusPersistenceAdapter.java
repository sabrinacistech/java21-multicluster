package com.company.clusteravailability.infrastructure.adapter.out.persistence;

import com.company.clusteravailability.domain.exception.ClusterStatusRepositoryException;
import com.company.clusteravailability.domain.model.ClusterAlias;
import com.company.clusteravailability.domain.model.ClusterStatus;
import com.company.clusteravailability.domain.model.PollingConfig;
import com.company.clusteravailability.domain.port.ClusterStatusRepositoryPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class ClusterStatusPersistenceAdapter implements ClusterStatusRepositoryPort {

    private final ClusterStatusJpaRepository repository;
    private final ClusterStatusMapper mapper;

    public ClusterStatusPersistenceAdapter(ClusterStatusJpaRepository repository, ClusterStatusMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Cacheable(cacheNames = "clusterStatusByAlias", key = "#alias.value()")
    @CircuitBreaker(name = "clusterStatusRepository", fallbackMethod = "fallbackFindByAlias")
    public Optional<ClusterStatus> findByAlias(ClusterAlias alias) {
        return repository.findFirstByClusterAlias(alias.value()).map(mapper::toDomain);
    }

    @Override
    @Cacheable(cacheNames = "clusterPollingConfigByAlias", key = "#alias.value()")
    @CircuitBreaker(name = "clusterStatusRepository", fallbackMethod = "fallbackFindPollingConfigByAlias")
    public Optional<PollingConfig> findPollingConfigByAlias(ClusterAlias alias) {
        return repository.findPollingConfigByClusterAlias(alias.value()).map(mapper::toDomain);
    }

    Optional<ClusterStatus> fallbackFindByAlias(ClusterAlias alias, Throwable throwable) {
        throw new ClusterStatusRepositoryException("Unable to read cluster status from persistence", throwable);
    }

    Optional<PollingConfig> fallbackFindPollingConfigByAlias(ClusterAlias alias, Throwable throwable) {
        throw new ClusterStatusRepositoryException("Unable to read cluster polling config from persistence", throwable);
    }
}
