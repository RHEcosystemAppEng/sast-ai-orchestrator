package com.redhat.sast.api.v1.resource.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.redhat.sast.api.model.OshUncollectedScan;
import com.redhat.sast.api.service.OshRetryService;
import com.redhat.sast.api.service.OshSchedulerService;
import com.redhat.sast.api.v1.dto.response.admin.OshRetryQueueResponseDto;
import com.redhat.sast.api.v1.dto.response.admin.OshRetryStatisticsResponseDto;
import com.redhat.sast.api.v1.dto.response.admin.OshStatusResponseDto;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * Admin REST endpoints for OSH (Open Scan Hub) integration monitoring and management.
 *
 * Provides visibility into:
 * - Retry queue status and contents
 * - Scheduler cursor position
 * - Retry statistics and performance metrics
 * - Manual cleanup operations
 */
@Path("/admin/osh")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class OshAdminResource {

    private static final String DEFAULT_QUEUE_LIMIT = "50";
    private static final int MIN_QUEUE_LIMIT = 1;
    private static final int MAX_QUEUE_LIMIT = 200;
    private static final String DEFAULT_SORT_BY = "created";

    @Inject
    OshRetryService oshRetryService;

    @Inject
    OshSchedulerService oshSchedulerService;

    /**
     * Get overall OSH integration status including retry queue and cursor information.
     *
     * @return comprehensive OSH status information
     */
    @GET
    @Path("/status")
    public Response getOshStatus() {
        try {
            OshStatusResponseDto status = new OshStatusResponseDto();

            status.setRetryQueueStatus(oshRetryService.getRetryQueueStatus());

            status.setCursorStatus(oshSchedulerService.getCursorStatus());

            status.setTimestamp(LocalDateTime.now());

            LOGGER.debug("OSH status requested via admin endpoint");
            return Response.ok(status).build();

        } catch (jakarta.persistence.PersistenceException e) {
            LOGGER.error("Database error retrieving OSH status: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error occurred")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving OSH status: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal server error")
                    .build();
        }
    }

    /**
     * Get detailed retry queue statistics.
     *
     * @return detailed retry statistics
     */
    @GET
    @Path("/retry/statistics")
    public Response getRetryStatistics() {
        try {
            OshRetryStatisticsResponseDto stats = buildRetryStatistics();

            LOGGER.debug("OSH retry statistics requested via admin endpoint");
            return Response.ok(stats).build();

        } catch (jakarta.persistence.PersistenceException e) {
            LOGGER.error("Database error retrieving retry statistics: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error occurred")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving retry statistics: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal server error")
                    .build();
        }
    }

    /**
     * Get retry queue contents.
     *
     * @param limit maximum number of entries to return (default: 50, max: 200)
     * @param sortBy sort field: "created" (default), "attempts", "last_attempt"
     * @return paginated retry queue contents
     */
    @GET
    @Path("/retry/queue")
    public Response getRetryQueue(
            @QueryParam("limit") @DefaultValue(DEFAULT_QUEUE_LIMIT) int limit,
            @QueryParam("sortBy") @DefaultValue(DEFAULT_SORT_BY) String sortBy) {
        try {
            int effectiveLimit = Math.clamp(limit, MIN_QUEUE_LIMIT, MAX_QUEUE_LIMIT);

            OshRetryQueueResponseDto queueResponse = buildRetryQueueResponse(effectiveLimit, sortBy);

            LOGGER.debug(
                    "OSH retry queue contents requested via admin endpoint (limit: {}, sortBy: {})",
                    effectiveLimit,
                    sortBy);
            return Response.ok(queueResponse).build();

        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid parameters for retry queue request: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid parameters: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve retry queue: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving retry queue: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Get retry information for a specific OSH scan ID.
     *
     * @param scanId OSH scan ID to look up
     * @return retry information if found
     */
    @GET
    @Path("/retry/scan/{scanId}")
    public Response getRetryInfo(@PathParam("scanId") Integer scanId) {
        try {
            if (scanId == null || scanId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid scan ID")
                        .build();
            }

            Optional<OshUncollectedScan> retryInfo = oshRetryService.findRetryInfo(scanId);

            if (retryInfo.isPresent()) {
                LOGGER.debug("Retry info requested for scan ID {} via admin endpoint", scanId);
                return Response.ok(retryInfo.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No retry information found for scan ID: " + scanId)
                        .build();
            }

        } catch (Exception e) {
            LOGGER.error("Failed to retrieve retry info for scan {}: {}", scanId, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving retry info: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Manually trigger cleanup of expired retry records.
     *
     * @return cleanup results
     */
    @POST
    @Path("/retry/cleanup-expired")
    public Response cleanupExpiredRetries() {
        try {
            oshRetryService.cleanupExpiredRetries();

            String result = "Expired retry cleanup completed successfully";
            LOGGER.info("Manual expired retry cleanup triggered via admin endpoint");

            return Response.ok()
                    .entity(new CleanupResult(result, LocalDateTime.now()))
                    .build();

        } catch (Exception e) {
            LOGGER.error("Failed to cleanup expired retries: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error during cleanup: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Manually trigger cleanup of retries that exceeded maximum attempts.
     *
     * @return cleanup results including number of records removed
     */
    @POST
    @Path("/retry/cleanup-exceeded")
    public Response cleanupExceededRetries() {
        try {
            int deletedCount = oshRetryService.cleanupExceededRetries();

            String result = String.format("Cleaned up %d retries that exceeded maximum attempts", deletedCount);
            LOGGER.info("Manual exceeded retry cleanup triggered via admin endpoint: {} records removed", deletedCount);

            return Response.ok()
                    .entity(new CleanupResult(result, LocalDateTime.now()))
                    .build();

        } catch (Exception e) {
            LOGGER.error("Failed to cleanup exceeded retries: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error during cleanup: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Manually trigger OSH polling cycle for testing purposes.
     *
     * @return polling results
     */
    @POST
    @Path("/polling/trigger")
    public Response triggerManualPoll() {
        try {
            String result = oshSchedulerService.manualPollOsh();

            LOGGER.info("Manual OSH poll triggered via admin endpoint");
            return Response.ok()
                    .entity(new PollResult(result, LocalDateTime.now()))
                    .build();

        } catch (Exception e) {
            LOGGER.error("Failed to trigger manual poll: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error triggering manual poll: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Builds comprehensive retry statistics for monitoring.
     */
    private OshRetryStatisticsResponseDto buildRetryStatistics() {
        OshRetryService.RetryQueueStatistics stats = oshRetryService.getDetailedRetryStatistics();

        OshRetryStatisticsResponseDto dto = new OshRetryStatisticsResponseDto();
        dto.setQueueStatus(oshRetryService.getRetryQueueStatus());
        dto.setTotalInQueue(stats.totalInQueue);
        dto.setEligibleForRetry(stats.eligibleForRetry);
        dto.setAwaitingBackoff(stats.awaitingBackoff);
        dto.setExceededMaxAttempts(stats.exceededMaxAttempts);
        dto.setConfigurationSummary(stats.configurationSummary);
        dto.setTimestamp(LocalDateTime.now());

        return dto;
    }

    /**
     * Builds retry queue response with filtering and sorting.
     */
    private OshRetryQueueResponseDto buildRetryQueueResponse(int limit, String sortBy) {
        OshRetryQueueResponseDto dto = new OshRetryQueueResponseDto();
        dto.setQueueStatus(oshRetryService.getRetryQueueStatus());
        dto.setRequestedLimit(limit);
        dto.setSortBy(sortBy);
        dto.setTimestamp(LocalDateTime.now());

        List<OshUncollectedScan> records = oshRetryService.getRetryQueueSnapshot(limit, sortBy);
        long totalCount = oshRetryService.getDetailedRetryStatistics().totalInQueue;

        dto.updatePaginationMetadata(records, totalCount);

        return dto;
    }

    /**
     * Simple DTO for cleanup operation results.
     */
    public static record CleanupResult(String message, LocalDateTime timestamp) {}

    /**
     * Simple DTO for manual poll operation results.
     */
    public static record PollResult(String message, LocalDateTime timestamp) {}
}
