package com.redhat.sast.api.v1.resource;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.redhat.sast.api.service.mlops.MlOpsBatchService;
import com.redhat.sast.api.v1.dto.request.MlOpsBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.response.MlOpsBatchResponseDto;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/mlops-batch")
@Tag(name = "MLOps Batches", description = "MLOps batch management operations")
@SuppressWarnings("java:S1192")
public class MlOpsBatchResource
        extends BaseBatchResource<MlOpsBatchSubmissionDto, MlOpsBatchResponseDto, MlOpsBatchService> {

    @Inject
    MlOpsBatchService mlOpsBatchService;

    @Override
    protected MlOpsBatchService getService() {
        return mlOpsBatchService;
    }

    @Override
    protected String getBatchTypeName() {
        return "MLOps batch";
    }

    @Override
    protected MlOpsBatchResponseDto submitBatchToService(@Valid MlOpsBatchSubmissionDto submissionDto) {
        return mlOpsBatchService.submitBatch(submissionDto);
    }

    @Override
    protected List<MlOpsBatchResponseDto> getAllBatchesFromService(int page, int size) {
        return mlOpsBatchService.getAllBatches(page, size);
    }

    @Override
    protected MlOpsBatchResponseDto getBatchByIdFromService(Long batchId) {
        return mlOpsBatchService.getBatchById(batchId);
    }

    @GET
    @Path("/{batchId}/detailed")
    @Operation(
            summary = "Get detailed MLOps batch",
            description = "Retrieves detailed information about a specific MLOps batch")
    @APIResponses(
            value = {
                @APIResponse(responseCode = "200", description = "Batch retrieved successfully"),
                @APIResponse(responseCode = "404", description = "Batch not found"),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response getBatchDetailed(
            @Parameter(description = "Batch ID", required = true) @PathParam("batchId") Long batchId) {
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
