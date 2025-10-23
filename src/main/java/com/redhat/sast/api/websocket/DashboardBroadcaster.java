package com.redhat.sast.api.websocket;

import java.util.Map;

import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for broadcasting dashboard events via WebSocket.
 * Called by JobService, JobBatchService, etc. when state changes.
 */
@ApplicationScoped
@Slf4j
public class DashboardBroadcaster {

    @Inject
    DashboardWebSocket websocket;

    public void broadcastJobStatusChange(JobResponseDto job) {
        LOGGER.debug("Broadcasting job status change: job={}, status={}", job.getJobId(), job.getStatus());
        websocket.broadcast(new DashboardWebSocket.WebSocketMessage("job_status_change", job));
    }

    public void broadcastBatchProgress(JobBatchResponseDto batch) {
        LOGGER.debug(
                "Broadcasting batch progress: batch={}, completed={}/{}",
                batch.getBatchId(),
                batch.getCompletedJobs(),
                batch.getTotalJobs());
        websocket.broadcast(new DashboardWebSocket.WebSocketMessage("batch_progress", batch));
    }

    public void broadcastOshScanCollected(Integer oshScanId, JobResponseDto job) {
        LOGGER.debug("Broadcasting OSH scan collected: scan={}, job={}", oshScanId, job.getJobId());
        websocket.broadcast(new DashboardWebSocket.WebSocketMessage(
                "osh_scan_collected", Map.of("oshScanId", oshScanId, "job", job)));
    }

    public void broadcastOshScanFailed(Integer oshScanId, String reason) {
        LOGGER.debug("Broadcasting OSH scan failed: scan={}, reason={}", oshScanId, reason);
        websocket.broadcast(new DashboardWebSocket.WebSocketMessage(
                "osh_scan_failed", Map.of("oshScanId", oshScanId, "reason", reason)));
    }

    public void broadcastSummaryUpdate(Map<String, Object> summary) {
        websocket.broadcast(new DashboardWebSocket.WebSocketMessage("summary_update", summary));
    }
}
