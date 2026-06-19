package com.company.clusteravailability.infrastructure.adapter.in.rest;

import com.company.clusteravailability.application.port.GetClusterStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cluster")
@Tag(name = "Cluster status", description = "Read the current in-memory cluster availability status")
public class ClusterStatusRestController {

    private final GetClusterStatusUseCase getClusterStatusUseCase;
    private final ClusterStatusRestMapper mapper;
    private final ApiResponseFactory responseFactory;

    public ClusterStatusRestController(
            GetClusterStatusUseCase getClusterStatusUseCase,
            ClusterStatusRestMapper mapper,
            ApiResponseFactory responseFactory
    ) {
        this.getClusterStatusUseCase = getClusterStatusUseCase;
        this.mapper = mapper;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/status")
    @Operation(
            summary = "Get current cluster status",
            description = "Returns the latest valid cluster status from memory without querying persistence.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cluster status available",
                            content = @Content(schema = @Schema(implementation = com.company.clusteravailability.infrastructure.adapter.in.rest.ApiResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Cluster status not found"),
                    @ApiResponse(responseCode = "503", description = "Cluster status unavailable"),
                    @ApiResponse(responseCode = "502", description = "Persistence dependency failed"),
                    @ApiResponse(responseCode = "500", description = "Unexpected server error")
            }
    )
    public ResponseEntity<com.company.clusteravailability.infrastructure.adapter.in.rest.ApiResponse<ClusterStatusResponse>> getStatus() {
        ClusterStatusResponse response = mapper.toResponse(getClusterStatusUseCase.getCurrentStatus());
        return ResponseEntity.ok(responseFactory.success(response));
    }
}
