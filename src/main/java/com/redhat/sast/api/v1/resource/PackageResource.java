package com.redhat.sast.api.v1.resource;

import java.util.List;

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
public class PackageResource {

    @Inject
    PackageService packageService;

    @GET
    public Response getAllPackages(
            @QueryParam("page") @DefaultValue("0") int page, @QueryParam("size") @DefaultValue("50") int size) {
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
    public Response getPackageSummary(@PathParam("packageName") String packageName) {
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
    public Response getPackageJobs(
            @PathParam("packageName") String packageName,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
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
