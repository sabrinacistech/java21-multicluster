package com.company.clusteravailability;

import com.company.clusteravailability.infrastructure.config.property.ClusterProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(ClusterProperties.class)
public class ClusterAvailabilityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClusterAvailabilityServiceApplication.class, args);
    }
}
