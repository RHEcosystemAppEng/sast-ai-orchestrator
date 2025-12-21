package com.redhat.sast.api.v1.resource;

import com.redhat.sast.api.enums.TimePeriod;
import com.redhat.sast.api.service.StatisticService;
import com.redhat.sast.api.v1.dto.response.DashboardSummaryDto;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
     * Get dashboard summary statistics filtered by time period.
     *
     * Returns aggregated counts for jobs, batches, and OSH scans within the specified time period.
     * Jobs are filtered by createdAt, batches by submittedAt.
     *
     * @param timePeriodCode time period code (1h, 6h, 12h, 24h, 7d, 30d). Defaults to 24h if not provided.
     * @return dashboard summary with job, batch, and OSH scan statistics for the time period
     */
    @GET
    @Path("/summary")
    public Response getDashboardSummary(@QueryParam("timePeriod") String timePeriodCode) {
        try {
            TimePeriod timePeriod = null;

            if (timePeriodCode != null && !timePeriodCode.isEmpty()) {
                timePeriod = TimePeriod.fromCode(timePeriodCode);
            } else {
                timePeriod = TimePeriod.TWENTY_FOUR_HOURS;
            }

            DashboardSummaryDto summary = statisticService.getSummary(timePeriod);

            LOGGER.debug("Dashboard summary requested for time period: {}", timePeriod.getCode());
            return Response.ok(summary).build();

        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid time period parameter: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
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
