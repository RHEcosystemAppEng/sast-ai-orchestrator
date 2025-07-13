package com.redhat.sast.api.v1.resource;

import java.util.List;

import com.redhat.sast.api.service.JobBatchService;
import com.redhat.sast.api.v1.dto.request.JobBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/job-batches")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobBatchResource {

    @Inject
    JobBatchService jobBatchService;

    @POST
    public Response submitBatch(@Valid JobBatchSubmissionDto submissionDto) {
        try {
            JobBatchResponseDto response = jobBatchService.submitBatch(submissionDto);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error submitting batch: " + e.getMessage())
                    .build();
        }
    }

    @GET
    public Response getAllBatches(
            @QueryParam("page") @DefaultValue("0") int page, @QueryParam("size") @DefaultValue("20") int size) {
        try {
            List<JobBatchResponseDto> batches = jobBatchService.getAllBatches(page, size);
            return Response.ok(batches).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving batches: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{batchId}")
    public Response getBatchById(@PathParam("batchId") Long batchId) {
        try {
            JobBatchResponseDto batch = jobBatchService.getBatchById(batchId);
            return Response.ok(batch).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Batch not found: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving batch: " + e.getMessage())
                    .build();
        }
    }
}
