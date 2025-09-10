package com.redhat.sast.api.v1.resource;

import com.redhat.sast.api.service.DatasetStorageService;
import com.redhat.sast.api.v1.dto.response.DatasetStorageHealthResponseDto;
import com.redhat.sast.api.v1.dto.response.DatasetStorageInitializationResponseDto;
import com.redhat.sast.api.v1.dto.response.ErrorResponseDto;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API endpoints for dataset storage management and monitoring.
 */
@Path("/api/v1/dataset-storage")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class DatasetStorageResource {

    @Inject
    DatasetStorageService datasetStorageService;

    /**
     * Health check endpoint for dataset storage.
     *
     * @return health status of dataset storage
     */
    @GET
    @Path("/health")
    public Response getStorageHealth() {
        try {
            DatasetStorageHealthResponseDto health = datasetStorageService.checkStorageHealth();
            return Response.ok(health).build();
        } catch (Exception e) {
            LOGGER.error("Failed to check dataset storage health", e);
            return Response.serverError()
                    .entity(new ErrorResponseDto("Failed to check storage health", e.getMessage()))
                    .build();
        }
    }

    /**
     * Initialize dataset storage PVCs.
     *
     * @return result of storage initialization
     */
    @POST
    @Path("/initialize")
    public Response initializeStorage() {
        try {
            boolean initialized = datasetStorageService.initializeDatasetStorage();
            if (initialized) {
                return Response.ok(new DatasetStorageInitializationResponseDto(
                                true, "Dataset storage initialized successfully"))
                        .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new DatasetStorageInitializationResponseDto(
                                false, "Failed to initialize dataset storage"))
                        .build();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize dataset storage", e);
            return Response.serverError()
                    .entity(new ErrorResponseDto("Failed to initialize storage", e.getMessage()))
                    .build();
        }
    }
}
