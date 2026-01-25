package com.redhat.sast.api.v1.resource;

import java.util.List;
import java.util.Locale;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.enums.TimePeriod;
import com.redhat.sast.api.service.JobService;
import com.redhat.sast.api.service.StatisticService;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.response.JobActivityDataPointDto;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Jobs", description = "SAST AI job management operations")
public class JobResource {

    private final JobService jobService;
    private final StatisticService statisticService;

    public JobResource(JobService jobService, StatisticService statisticService) {
        this.jobService = jobService;
        this.statisticService = statisticService;
    }

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
    @Operation(
            summary = "Create a new SAST job",
            description = "Creates a new SAST analysis job or returns cached/running job")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "201",
                        description = "Job created successfully",
                        content = @Content(schema = @Schema(implementation = JobResponseDto.class))),
                @APIResponse(
                        responseCode = "200",
                        description = "Cached result or existing running job returned",
                        content = @Content(schema = @Schema(implementation = JobResponseDto.class))),
                @APIResponse(responseCode = "400", description = "Invalid request")
            })
    public Response createJobSimple(
            @Valid JobCreationDto jobCreationDto,
            @Parameter(description = "Force rescan even if cached result exists")
                    @QueryParam("forceRescan")
                    @DefaultValue("false")
                    boolean forceRescan) {
        try {
            // Allow forceRescan via query param as well as request body
            if (forceRescan && !Boolean.TRUE.equals(jobCreationDto.getForceRescan())) {
                jobCreationDto.setForceRescan(true);
            }

            JobResponseDto response = jobService.createJob(jobCreationDto);

            // Return appropriate HTTP status based on result type:
            // - 201 Created: New job was created
            // - 200 OK: Cached result or existing running job returned
            if (response.isCachedResult() || response.isExistingRun()) {
                return Response.status(Response.Status.OK).entity(response).build();
            }
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error creating job: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Operation(summary = "Get all jobs", description = "Retrieves all jobs with optional filtering and pagination")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        description = "Jobs retrieved successfully",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation = JobResponseDto.class,
                                                        type = SchemaType.ARRAY))),
                @APIResponse(responseCode = "400", description = "Invalid parameters"),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response getAllJobs(
            @Parameter(description = "Filter by package name") @QueryParam("packageName") String packageName,
            @Parameter(description = "Filter by job status") @QueryParam("status") String statusStr,
            @Parameter(description = "Page number (0-based)") @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Page size") @QueryParam("size") @DefaultValue("20") int size) {
        try {
            JobStatus status = null;
            if (statusStr != null && !statusStr.isEmpty()) {
                status = JobStatus.valueOf(statusStr.toUpperCase(Locale.ROOT));
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
    @Operation(summary = "Get job by ID", description = "Retrieves a specific job by its ID")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        description = "Job retrieved successfully",
                        content = @Content(schema = @Schema(implementation = JobResponseDto.class))),
                @APIResponse(responseCode = "404", description = "Job not found"),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response getJobById(@Parameter(description = "Job ID", required = true) @PathParam("jobId") Long jobId) {
        try {
            JobResponseDto job = jobService.getJobDtoByJobId(jobId);
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
    @Path("/{jobId}/cancel")
    @Operation(summary = "Cancel a job", description = "Cancels a running job")
    @APIResponses(
            value = {
                @APIResponse(responseCode = "200", description = "Job cancellation requested"),
                @APIResponse(responseCode = "404", description = "Job not found"),
                @APIResponse(responseCode = "400", description = "Job cannot be cancelled"),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response cancelJob(@Parameter(description = "Job ID", required = true) @PathParam("jobId") Long jobId) {
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

    @GET
    @Path("/activity/{timePeriod}")
    public Response getJobActivity(@PathParam("timePeriod") String timePeriodCode) {
        try {
            TimePeriod timePeriod = TimePeriod.fromCode(timePeriodCode);
            List<JobActivityDataPointDto> activity = statisticService.getJobActivity(timePeriod);
            return Response.ok(activity).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving job activity: " + e.getMessage())
                    .build();
        }
    }
}
