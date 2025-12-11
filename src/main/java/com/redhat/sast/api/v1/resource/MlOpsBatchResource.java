package com.redhat.sast.api.v1.resource;

import java.util.List;

import com.redhat.sast.api.service.mlops.MlOpsBatchService;
import com.redhat.sast.api.v1.dto.request.MlOpsBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.response.MlOpsBatchResponseDto;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/mlops-batch")
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
