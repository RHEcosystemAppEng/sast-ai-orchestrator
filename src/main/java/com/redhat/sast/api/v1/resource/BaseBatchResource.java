package com.redhat.sast.api.v1.resource;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for batch resource endpoints that provides common REST patterns
 * and error handling for both JobBatch and MLOpsBatch resources.
 *
 * @param <SubmissionDto> The submission DTO type for creating batches
 * @param <ResponseDto> The response DTO type for batch responses
 * @param <Service> The service type that handles batch operations
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public abstract class BaseBatchResource<SubmissionDto, ResponseDto, Service> {

    /**
     * Returns the service instance that handles batch operations.
     */
    protected abstract Service getService();

    /**
     * Returns a descriptive name for this batch type (e.g., "batch", "MLOps batch").
     */
    protected abstract String getBatchTypeName();

    /**
     * Delegates the batch submission to the service.
     */
    protected abstract ResponseDto submitBatchToService(@Valid SubmissionDto submissionDto);

    /**
     * Delegates retrieving all batches to the service.
     */
    protected abstract List<ResponseDto> getAllBatchesFromService(int page, int size);

    /**
     * Delegates retrieving a batch by ID to the service.
     */
    protected abstract ResponseDto getBatchByIdFromService(Long batchId);

    @POST
    public Response submitBatch(@Valid SubmissionDto submissionDto) {
        try {
            ResponseDto response = submitBatchToService(submissionDto);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            LOGGER.error("Error submitting {}: {}", getBatchTypeName(), e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(String.format("Error submitting %s: %s", getBatchTypeName(), e.getMessage()))
                    .build();
        }
    }

    @GET
    public Response getAllBatches(
            @QueryParam("page") @DefaultValue("0") int page, @QueryParam("size") @DefaultValue("20") int size) {
        try {
            List<ResponseDto> batches = getAllBatchesFromService(page, size);
            return Response.ok(batches).build();
        } catch (Exception e) {
            LOGGER.error("Error retrieving {}es: {}", getBatchTypeName(), e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("Error retrieving %ses: %s", getBatchTypeName(), e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{batchId}")
    public Response getBatchById(@PathParam("batchId") Long batchId) {
        try {
            ResponseDto batch = getBatchByIdFromService(batchId);
            if (batch == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("%s not found with ID: %d", getBatchTypeName(), batchId))
                        .build();
            }
            return Response.ok(batch).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(String.format("%s not found: %s", getBatchTypeName(), e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error retrieving {}: {}", getBatchTypeName(), e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("Error retrieving %s: %s", getBatchTypeName(), e.getMessage()))
                    .build();
        }
    }
}
