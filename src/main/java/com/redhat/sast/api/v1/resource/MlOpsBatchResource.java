package com.redhat.sast.api.v1.resource;

import java.util.List;

import com.redhat.sast.api.service.MlOpsBatchService;
import com.redhat.sast.api.v1.dto.request.MlOpsBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.response.MlOpsBatchResponseDto;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/mlops-batch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MlOpsBatchResource {

    @Inject
    MlOpsBatchService mlOpsBatchService;

    @POST
    public Response submitBatch(@Valid MlOpsBatchSubmissionDto submissionDto) {
        try {
            MlOpsBatchResponseDto response = mlOpsBatchService.submitBatch(submissionDto);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error submitting MLOps batch: " + e.getMessage())
                    .build();
        }
    }

    @GET
    public Response getAllBatches(
            @QueryParam("page") @DefaultValue("0") int page, @QueryParam("size") @DefaultValue("20") int size) {
        try {
            List<MlOpsBatchResponseDto> batches = mlOpsBatchService.getAllBatches(page, size);
            return Response.ok(batches).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving MLOps batches: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{batchId}")
    public Response getBatchById(@PathParam("batchId") Long batchId) {
        try {
            MlOpsBatchResponseDto batch = mlOpsBatchService.getBatchById(batchId);
            if (batch == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("MLOps batch not found with ID: " + batchId)
                        .build();
            }
            return Response.ok(batch).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving MLOps batch: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{batchId}/detailed")
    public Response getBatchDetailed(@PathParam("batchId") Long batchId) {
        try {
            com.redhat.sast.api.v1.dto.response.MlOpsBatchDetailedResponseDto batch =
                    mlOpsBatchService.getBatchDetailedById(batchId);
            if (batch == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("MLOps batch not found with ID: " + batchId)
                        .build();
            }
            return Response.ok(batch).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving detailed MLOps batch: " + e.getMessage())
                    .build();
        }
    }
}
