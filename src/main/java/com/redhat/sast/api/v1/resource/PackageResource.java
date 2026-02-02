package com.redhat.sast.api.v1.resource;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.redhat.sast.api.service.PackageService;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;
import com.redhat.sast.api.v1.dto.response.MonitoredPackageWithScansDto;
import com.redhat.sast.api.v1.dto.response.PackageSummaryDto;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/packages")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Packages", description = "Package management and statistics")
@SuppressWarnings("java:S1192")
public class PackageResource {

    @Inject
    PackageService packageService;

    @GET
    @Operation(summary = "Get all packages", description = "Retrieves all packages with pagination")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        description = "Packages retrieved successfully",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation = PackageSummaryDto.class,
                                                        type = SchemaType.ARRAY))),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response getAllPackages(
            @Parameter(description = "Page number (0-based)") @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Page size") @QueryParam("size") @DefaultValue("50") int size) {
        try {
            List<PackageSummaryDto> packages = packageService.getAllPackages(page, size);
            return Response.ok(packages).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving packages: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{packageName}")
    @Operation(summary = "Get package summary", description = "Retrieves summary information for a specific package")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        description = "Package summary retrieved successfully",
                        content = @Content(schema = @Schema(implementation = PackageSummaryDto.class))),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response getPackageSummary(
            @Parameter(description = "Package name", required = true) @PathParam("packageName") String packageName) {
        try {
            PackageSummaryDto summary = packageService.getPackageSummary(packageName);
            return Response.ok(summary).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving package summary: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{packageName}/jobs")
    @Operation(summary = "Get package jobs", description = "Retrieves all jobs for a specific package")
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
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response getPackageJobs(
            @Parameter(description = "Package name", required = true) @PathParam("packageName") String packageName,
            @Parameter(description = "Page number (0-based)") @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Page size") @QueryParam("size") @DefaultValue("20") int size) {
        try {
            List<JobResponseDto> jobs = packageService.getPackageJobs(packageName, page, size);
            return Response.ok(jobs).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving package jobs: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/monitored-with-scans")
    @Operation(
            summary = "Get monitored packages with scans",
            description = "Retrieves all monitored packages along with their scan information")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        description = "Monitored packages retrieved successfully",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation = MonitoredPackageWithScansDto.class,
                                                        type = SchemaType.ARRAY))),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response getMonitoredPackagesWithScans() {
        try {
            List<MonitoredPackageWithScansDto> packages = packageService.getMonitoredPackagesWithScans();
            return Response.ok(packages).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving monitored packages with scans: " + e.getMessage())
                    .build();
        }
    }
}
