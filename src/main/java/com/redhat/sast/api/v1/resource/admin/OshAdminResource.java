package com.redhat.sast.api.v1.resource.admin;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.redhat.sast.api.model.OshUncollectedScan;
import com.redhat.sast.api.service.StatisticService;
import com.redhat.sast.api.service.osh.OshClientService;
import com.redhat.sast.api.service.osh.OshJobCreationService;
import com.redhat.sast.api.service.osh.OshRetryService;
import com.redhat.sast.api.startup.OshScheduler;
import com.redhat.sast.api.v1.dto.response.OshScanStatusDto;
import com.redhat.sast.api.v1.dto.response.admin.OshManualCollectionResponseDto;
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
@Tag(name = "OSH Admin", description = "OSH (Open Scan Hub) integration admin operations")
@Slf4j
public class OshAdminResource {

    private static final String DEFAULT_QUEUE_LIMIT = "50";
    private static final int MIN_QUEUE_LIMIT = 1;
    private static final int MAX_QUEUE_LIMIT = 200;
    private static final String DEFAULT_SORT_BY = "created";
    private static final String DATABASE_ERROR_OCCURRED = "Database error occurred";

    @Inject
    OshRetryService oshRetryService;

    @Inject
    OshScheduler oshScheduler;

    @Inject
    StatisticService statisticService;

    @Inject
    OshClientService oshClientService;

    @Inject
    OshJobCreationService oshJobCreationService;

    /**
     * Get overall OSH integration status including retry queue and cursor information.
     *
     * @return comprehensive OSH status information
     */
    @GET
    @Path("/status")
    @Operation(
            summary = "Get OSH Status",
            description = "Get overall OSH integration status including retry queue and cursor information")
    @APIResponses(
            value = {
                @APIResponse(responseCode = "200", description = "OSH status retrieved successfully"),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response getOshStatus() {
        try {
            OshStatusResponseDto status = new OshStatusResponseDto();

            status.setRetryQueueStatus(oshRetryService.getRetryQueueStatus());

            status.setCursorStatus(oshScheduler.getCursorStatus());

            status.setTimestamp(Instant.now());

            LOGGER.debug("OSH status requested via admin endpoint");
            return Response.ok(status).build();

        } catch (jakarta.persistence.PersistenceException e) {
            LOGGER.error("Database error retrieving OSH status: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(DATABASE_ERROR_OCCURRED)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving OSH status: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal server error")
                    .build();
        }
    }

    /**
     * Get all OSH scans (both collected and uncollected) with pagination and filtering.
     *
     * @param page page number
     * @param size page size
     * @param status filter by status: "COLLECTED", "UNCOLLECTED", or null for all
     * @return paginated list of OSH scans with job and retry information
     */
    @GET
    @Path("/scans/all")
    public Response getAllOshScans(
            @QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("size") @DefaultValue("20") Integer size,
            @QueryParam("status") String status) {
        try {
            if (status != null && !status.isEmpty() && !status.equals("COLLECTED") && !status.equals("UNCOLLECTED")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid status parameter. Must be 'COLLECTED' or 'UNCOLLECTED'")
                        .build();
            }

            List<OshScanStatusDto> scans = statisticService.getAllOshScans(page, size, status);

            return Response.ok(scans).build();

        } catch (jakarta.persistence.PersistenceException e) {
            LOGGER.error("Database error retrieving OSH scans: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(DATABASE_ERROR_OCCURRED)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving OSH scans: {}", e.getMessage(), e);
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
                    .entity(DATABASE_ERROR_OCCURRED)
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
                    .entity(new CleanupResult(result, Instant.now()))
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
                    .entity(new CleanupResult(result, Instant.now()))
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
    @Operation(
            summary = "Trigger Manual OSH Poll",
            description = "Manually trigger OSH polling cycle for testing purposes")
    @APIResponses(
            value = {
                @APIResponse(responseCode = "200", description = "Poll triggered successfully"),
                @APIResponse(responseCode = "500", description = "Internal server error")
            })
    public Response triggerManualPoll() {
        try {
            String result = oshScheduler.manualPollOsh();

            LOGGER.info("Manual OSH poll triggered via admin endpoint");
            return Response.ok().entity(new PollResult(result, Instant.now())).build();

        } catch (Exception e) {
            LOGGER.error("Failed to trigger manual poll: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error triggering manual poll: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Test endpoint to collect a specific OSH scan by ID and create a workflow job.
     *
     * This endpoint:
     * 1. Fetches scan metadata from OSH API
     * 2. Validates the scan can be processed
     * 3. Creates a SAST-AI workflow job if eligible
     *
     * @param scanId OSH scan ID to collect
     * @return job creation result with scan and job data
     */
    @POST
    @Path("/test/collect-scan/{scanId}")
    public Response testCollectScan(@PathParam("scanId") Integer scanId) {
        try {
            if (scanId == null || scanId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid scan ID")
                        .build();
            }

            LOGGER.info("Manual collection triggered for OSH scan ID: {}", scanId);
            var scanDataOpt = oshClientService.fetchOshScanData(scanId);

            if (scanDataOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Scan not found or could not be collected: " + scanId)
                        .build();
            }

            var scanData = scanDataOpt.get();
            LOGGER.info("Successfully collected scan data for ID {}: {}", scanId, scanData);

            if (!oshJobCreationService.canProcessScan(scanData)) {
                return Response.ok(new OshManualCollectionResponseDto(
                                scanData,
                                null,
                                false,
                                "Scan cannot be processed (not in CLOSED state or missing required data)"))
                        .build();
            }

            var jobOpt = oshJobCreationService.createJobFromOshScan(scanData);

            if (jobOpt.isPresent()) {
                var job = jobOpt.get();
                LOGGER.info("Successfully created job {} for OSH scan {}", job.getId(), scanId);
                return Response.ok(new OshManualCollectionResponseDto(scanData, job, true, "Job created successfully"))
                        .build();
            } else {
                return Response.ok(new OshManualCollectionResponseDto(
                                scanData, null, false, "Scan already processed or JSON data not available"))
                        .build();
            }

        } catch (Exception e) {
            LOGGER.error("Failed to collect/process scan {}: {}", scanId, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error collecting scan: " + e.getMessage())
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
        dto.setTimestamp(Instant.now());

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
        dto.setTimestamp(Instant.now());

        List<OshUncollectedScan> records = oshRetryService.getRetryQueueSnapshot(limit, sortBy);
        long totalCount = oshRetryService.getDetailedRetryStatistics().totalInQueue;

        dto.updatePaginationMetadata(records, totalCount);

        return dto;
    }

    /**
     * Simple DTO for cleanup operation results.
     */
    public static record CleanupResult(String message, Instant timestamp) {}

    /**
     * Simple DTO for manual poll operation results.
     */
    public static record PollResult(String message, Instant timestamp) {}
}
