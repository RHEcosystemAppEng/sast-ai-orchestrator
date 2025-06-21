package com.redhat.sast.api.v1.resource;

import java.util.List;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.service.JobService;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobResource {

    @Inject
    JobService jobService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createJob(@FormParam("metadata") String metadataJson, @FormParam("inputFile") String inputFile) {
        // Note: File handling for SARIF files would be implemented here
        // For now, we're handling the metadata part as JSON string
        try {
            // TODO: Add proper JSON parsing here
            // ObjectMapper mapper = new ObjectMapper();
            // JobCreationDto metadata = mapper.readValue(metadataJson, JobCreationDto.class);

            // For now, return a simple error message indicating this endpoint needs implementation
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                    .entity(
                            "Multipart file upload endpoint not yet implemented. Use /simple endpoint for JSON-only job creation.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error creating job: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/simple")
    public Response createJobSimple(JobCreationDto jobCreationDto) {
        // Simplified endpoint for JSON-only job creation
        try {
            JobResponseDto response = jobService.createJob(jobCreationDto);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error creating job: " + e.getMessage())
                    .build();
        }
    }

    @GET
    public Response getAllJobs(
            @QueryParam("packageName") String packageName,
            @QueryParam("status") String statusStr,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        try {
            JobStatus status = null;
            if (statusStr != null && !statusStr.isEmpty()) {
                status = JobStatus.valueOf(statusStr.toUpperCase());
            }

            List<JobResponseDto> jobs = jobService.getAllJobs(packageName, status, page, size);
            return Response.ok(jobs).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid status parameter: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving jobs: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{jobId}")
    public Response getJobById(@PathParam("jobId") Long jobId) {
        try {
            JobResponseDto job = jobService.getJobById(jobId);
            return Response.ok(job).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Job not found: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving job: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/{jobId}:cancel")
    public Response cancelJob(@PathParam("jobId") Long jobId) {
        try {
            jobService.cancelJob(jobId);
            return Response.ok("Job cancellation requested").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Job not found: " + e.getMessage())
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot cancel job: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error cancelling job: " + e.getMessage())
                    .build();
        }
    }
}
