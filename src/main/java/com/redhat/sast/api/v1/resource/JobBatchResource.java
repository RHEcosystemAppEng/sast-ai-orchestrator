package com.redhat.sast.api.v1.resource;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.redhat.sast.api.service.JobBatchService;
import com.redhat.sast.api.v1.dto.request.JobBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/job-batches")
@Tag(name = "Job Batches", description = "Job batch management operations")
public class JobBatchResource extends BaseBatchResource<JobBatchSubmissionDto, JobBatchResponseDto, JobBatchService> {

    @Inject
    JobBatchService jobBatchService;

    @Override
    protected JobBatchService getService() {
        return jobBatchService;
    }

    @Override
    protected String getBatchTypeName() {
        return "batch";
    }

    @Override
    protected JobBatchResponseDto submitBatchToService(@Valid JobBatchSubmissionDto submissionDto) {
        return jobBatchService.submitBatch(submissionDto);
    }

    @Override
    protected List<JobBatchResponseDto> getAllBatchesFromService(int page, int size) {
        return jobBatchService.getAllBatches(page, size);
    }

    @Override
    protected JobBatchResponseDto getBatchByIdFromService(Long batchId) {
        try {
            return jobBatchService.getBatchById(batchId);
        } catch (IllegalArgumentException e) {
            return null; // BaseBatchResource will handle null as NOT_FOUND
        }
    }

    @POST
    @Path("/{batchId}/cancel")
    @Operation(summary = "Cancel a job batch", description = "Cancels all jobs in a batch")
    @APIResponses(
            value = {
                @APIResponse(responseCode = "200", description = "Batch cancellation requested"),
                @APIResponse(responseCode = "404", description = "Batch not found"),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response cancelJobBatch(
            @Parameter(description = "Batch ID", required = true) @PathParam("batchId") Long batchId) {
        try {
            jobBatchService.cancelJobBatch(batchId);
            return Response.ok("Job batch cancellation requested").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Job batch not found: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to cancel job batch: " + e.getMessage())
                    .build();
        }
    }
}
