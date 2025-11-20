package com.redhat.sast.api.v1.resource;

import com.redhat.sast.api.service.StatisticService;
import com.redhat.sast.api.v1.dto.response.DashboardSummaryDto;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST endpoint for dashboard summary statistics.
 *
 */
@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class DashboardResource {

    private final StatisticService statisticService;

    /**
     * Get dashboard summary statistics.
     *
     * Returns aggregated counts for jobs, batches, and OSH scans.
     *
     * @return dashboard summary with job, batch, and OSH scan statistics
     */
    @GET
    @Path("/summary")
    public Response getDashboardSummary() {
        try {
            DashboardSummaryDto summary = statisticService.getSummary();

            LOGGER.debug("Dashboard summary requested");
            return Response.ok(summary).build();

        } catch (jakarta.persistence.PersistenceException e) {
            LOGGER.error("Database error retrieving dashboard summary: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error occurred")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving dashboard summary: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal server error")
                    .build();
        }
    }
}
