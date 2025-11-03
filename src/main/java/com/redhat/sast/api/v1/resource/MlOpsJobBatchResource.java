package com.redhat.sast.api.v1.resource;

import java.util.List;

import com.redhat.sast.api.service.MlOpsBatchService;
import com.redhat.sast.api.v1.dto.request.MlOpsJobBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource for MLOps job batch operations.
 * Provides endpoints for submitting and managing MLOps pipeline batches.
 */
@Path("/mlops-batches")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MlOpsJobBatchResource {

    @Inject
    MlOpsBatchService mlOpsBatchService;

    /**
     * Submit a new MLOps batch job for processing.
     *
     * @param submissionDto the MLOps batch submission data including DVC and S3 configuration
     * @return Response containing the created batch information
     */
    @POST
    public Response submitMlOpsBatch(@Valid MlOpsJobBatchSubmissionDto submissionDto) {
        try {
            JobBatchResponseDto response = mlOpsBatchService.submitMlOpsBatch(submissionDto);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error submitting MLOps batch: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Retrieve all MLOps batches with pagination.
     *
     * @param page the page number (default: 0)
     * @param size the page size (default: 20)
     * @return Response containing the list of MLOps batches
     */
    @GET
    public Response getAllMlOpsBatches(
            @QueryParam("page") @DefaultValue("0") int page, @QueryParam("size") @DefaultValue("20") int size) {
        try {
            List<JobBatchResponseDto> batches = mlOpsBatchService.getAllBatches(page, size);
            return Response.ok(batches).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving MLOps batches: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Retrieve a specific MLOps batch by ID.
     *
     * @param batchId the ID of the batch to retrieve
     * @return Response containing the batch information
     */
    @GET
    @Path("/{batchId}")
    public Response getMlOpsBatchById(@PathParam("batchId") Long batchId) {
        try {
            JobBatchResponseDto batch = mlOpsBatchService.getBatchById(batchId);
            return Response.ok(batch).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("MLOps batch not found: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving MLOps batch: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Cancel an MLOps batch job.
     *
     * @param batchId the ID of the batch to cancel
     * @return Response indicating the cancellation status
     */
    @POST
    @Path("/{batchId}/cancel")
    public Response cancelMlOpsJobBatch(@PathParam("batchId") Long batchId) {
        try {
            mlOpsBatchService.cancelJobBatch(batchId);
            return Response.ok("MLOps job batch cancellation requested").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("MLOps job batch not found: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to cancel MLOps job batch: " + e.getMessage())
                    .build();
        }
    }
}

