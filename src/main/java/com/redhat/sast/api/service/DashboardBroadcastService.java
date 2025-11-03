package com.redhat.sast.api.service;

import java.util.Map;

import com.redhat.sast.api.mapper.JobMapper;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.v1.dto.response.DashboardSummaryDto;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;
import com.redhat.sast.api.websocket.DashboardWebSocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for broadcasting real-time updates to dashboard WebSocket clients.
 *
 * Handles conversion of domain entities to DTOs and message formatting
 * before broadcasting through the WebSocket endpoint.
 */
@ApplicationScoped
@Slf4j
public class DashboardBroadcastService {

    @Inject
    DashboardWebSocket dashboardWebSocket;

    @Inject
    DashboardService dashboardService;

    /**
     * Broadcasts a job status change to all connected clients.
     *
     * @param job the job that changed status
     */
    public void broadcastJobStatusChange(Job job) {
        try {
            JobResponseDto jobDto = JobMapper.INSTANCE.jobToJobResponseDto(job);
            var message = new DashboardWebSocket.WebSocketMessage("job_status_change", jobDto);
            dashboardWebSocket.broadcast(message);

            LOGGER.debug("Broadcasted job status change for job ID: {} (status: {})", job.getId(), job.getStatus());

            dashboardService.invalidateCache();

        } catch (Exception e) {
            LOGGER.error("Failed to broadcast job status change for job ID {}: {}", job.getId(), e.getMessage(), e);
        }
    }

    /**
     * Broadcasts a batch progress update to all connected clients.
     *
     * @param jobBatch the batch with updated progress
     */
    public void broadcastBatchProgress(JobBatch jobBatch) {
        try {
            var message = new DashboardWebSocket.WebSocketMessage("batch_progress", jobBatch);
            dashboardWebSocket.broadcast(message);

            LOGGER.debug(
                    "Broadcasted batch progress for batch ID: {} (completed: {}/{})",
                    jobBatch.getId(),
                    jobBatch.getCompletedJobs(),
                    jobBatch.getTotalJobs());

            dashboardService.invalidateCache();

        } catch (Exception e) {
            LOGGER.error("Failed to broadcast batch progress for batch ID {}: {}", jobBatch.getId(), e.getMessage(), e);
        }
    }

    /**
     * Broadcasts dashboard summary update to all connected clients.
     */
    public void broadcastSummaryUpdate() {
        try {
            dashboardService.invalidateCache();
            DashboardSummaryDto summary = dashboardService.getSummary();

            var message = new DashboardWebSocket.WebSocketMessage("summary_update", summary);
            dashboardWebSocket.broadcast(message);

            LOGGER.debug("Broadcasted dashboard summary update");

        } catch (Exception e) {
            LOGGER.error("Failed to broadcast dashboard summary update: {}", e.getMessage(), e);
        }
    }

    /**
     * Broadcasts OSH scan collection success to all connected clients.
     *
     * @param job the newly created job from the collected OSH scan
     */
    public void broadcastOshScanCollected(Job job) {
        try {
            JobResponseDto jobDto = JobMapper.INSTANCE.jobToJobResponseDto(job);
            var message = new DashboardWebSocket.WebSocketMessage("osh_scan_collected", Map.of("job", jobDto));
            dashboardWebSocket.broadcast(message);

            LOGGER.info(
                    "Broadcasted OSH scan collection success for job ID: {} (OSH scan ID: {})",
                    job.getId(),
                    job.getOshScanId());

            dashboardService.invalidateCache();

        } catch (Exception e) {
            LOGGER.error("Failed to broadcast OSH scan collection for job ID {}: {}", job.getId(), e.getMessage(), e);
        }
    }

    /**
     * Broadcasts OSH scan collection failure to all connected clients.
     *
     * @param oshScanId the OSH scan ID that failed
     * @param failureReason the reason for failure
     * @param retryAttempts the number of retry attempts made
     */
    public void broadcastOshScanFailed(String oshScanId, String failureReason, Integer retryAttempts) {
        try {
            var message = new DashboardWebSocket.WebSocketMessage(
                    "osh_scan_failed",
                    Map.of("oshScanId", oshScanId, "failureReason", failureReason, "retryAttempts", retryAttempts));
            dashboardWebSocket.broadcast(message);

            LOGGER.info(
                    "Broadcasted OSH scan failure for scan ID: {} (reason: {}, attempts: {})",
                    oshScanId,
                    failureReason,
                    retryAttempts);

            dashboardService.invalidateCache();

        } catch (Exception e) {
            LOGGER.error("Failed to broadcast OSH scan failure for scan ID {}: {}", oshScanId, e.getMessage(), e);
        }
    }

    /**
     * Gets the current number of active WebSocket connections.
     *
     * @return number of active connections
     */
    public int getActiveConnectionCount() {
        return dashboardWebSocket.getActiveConnectionCount();
    }
}
