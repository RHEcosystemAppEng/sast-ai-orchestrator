package com.redhat.sast.api.v1.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

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
@Tag(name = "Dashboard", description = "Dashboard statistics and summaries")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("java:S1192")
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
    @Operation(
            summary = "Get dashboard summary",
            description = "Returns aggregated counts for jobs, batches, and OSH scans within the specified time period")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        description = "Dashboard summary retrieved successfully",
                        content = @Content(schema = @Schema(implementation = DashboardSummaryDto.class))),
                @APIResponse(responseCode = "400", description = "Invalid time period parameter"),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response getDashboardSummary(
            @Parameter(description = "Time period code (1h, 6h, 12h, 24h, 7d, 30d). Defaults to 24h")
                    @QueryParam("timePeriod")
                    String timePeriodCode) {
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
