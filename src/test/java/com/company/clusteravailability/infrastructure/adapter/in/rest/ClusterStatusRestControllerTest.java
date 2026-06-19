package com.company.clusteravailability.infrastructure.adapter.in.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.clusteravailability.application.port.GetClusterStatusUseCase;
import com.company.clusteravailability.domain.exception.ClusterStatusUnavailableException;
import com.company.clusteravailability.domain.model.ClusterAlias;
import com.company.clusteravailability.domain.model.ClusterStatus;
import com.company.clusteravailability.domain.model.PollingIntervalSeconds;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ClusterStatusRestControllerTest {

    @Test
    void returnsCurrentStatusFromUseCase() throws Exception {
        GetClusterStatusUseCase useCase = org.mockito.Mockito.mock(GetClusterStatusUseCase.class);
        when(useCase.getCurrentStatus()).thenReturn(sampleStatus());
        ApiResponseFactory factory = new ApiResponseFactory(fixedClock(), "cluster-availability-service");
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ClusterStatusRestController(useCase, new ClusterStatusRestMapper(), factory))
                .setControllerAdvice(new GlobalExceptionHandler(factory))
                .build();

        mockMvc.perform(get("/api/v1/cluster/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alias").value("primary-cluster"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void mapsUnavailableTo503() throws Exception {
        GetClusterStatusUseCase useCase = org.mockito.Mockito.mock(GetClusterStatusUseCase.class);
        when(useCase.getCurrentStatus()).thenThrow(new ClusterStatusUnavailableException());
        ApiResponseFactory factory = new ApiResponseFactory(fixedClock(), "cluster-availability-service");
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ClusterStatusRestController(useCase, new ClusterStatusRestMapper(), factory))
                .setControllerAdvice(new GlobalExceptionHandler(factory))
                .build();

        mockMvc.perform(get("/api/v1/cluster/status"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errors[0].code").value("CLUSTER_STATUS_UNAVAILABLE"));
    }

    private static ClusterStatus sampleStatus() {
        return new ClusterStatus(
                new ClusterAlias("primary-cluster"),
                true,
                new PollingIntervalSeconds(30),
                Instant.parse("2026-06-19T10:15:00Z")
        );
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-06-19T10:15:30Z"), ZoneOffset.UTC);
    }
}
