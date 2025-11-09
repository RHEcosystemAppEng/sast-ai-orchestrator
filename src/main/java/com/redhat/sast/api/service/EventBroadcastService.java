package com.redhat.sast.api.service;

import com.redhat.sast.api.event.BatchProgressEvent;
import com.redhat.sast.api.event.JobStatusChangedEvent;
import com.redhat.sast.api.event.OshScanCollectedEvent;
import com.redhat.sast.api.event.OshScanFailedEvent;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for broadcasting real-time updates using CDI events.
 *
 * Fires CDI events that are observed by interested listeners such as:
 * - WebSocketResource for dashboard updates
 * - Logging/metrics services
 * - Downstream workflow triggers
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class EventBroadcastService {

    private final Event<JobStatusChangedEvent> jobStatusChangedEvent;
    private final Event<BatchProgressEvent> batchProgressEvent;
    private final Event<OshScanCollectedEvent> oshScanCollectedEvent;
    private final Event<OshScanFailedEvent> oshScanFailedEvent;

    /**
     * Broadcasts a job status change to all connected clients.
     *
     * @param job the job that changed status
     */
    public void broadcastJobStatusChange(Job job) {
        try {
            jobStatusChangedEvent.fire(new JobStatusChangedEvent(job));
        } catch (Exception e) {
            LOGGER.error("Failed to fire job status change event for job ID {}: {}", job.getId(), e.getMessage(), e);
        }
    }

    /**
     * Broadcasts a batch progress update to all connected clients.
     *
     * @param jobBatch the batch with updated progress
     */
    public void broadcastBatchProgress(JobBatch jobBatch) {
        try {
            batchProgressEvent.fire(new BatchProgressEvent(jobBatch));
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to fire batch progress event for batch ID {}: {}", jobBatch.getId(), e.getMessage(), e);
        }
    }

    /**
     * Broadcasts OSH scan collection success to all connected clients.
     *
     * @param job the newly created job from the collected OSH scan
     */
    public void broadcastOshScanCollected(Job job) {
        try {
            oshScanCollectedEvent.fire(new OshScanCollectedEvent(job));
        } catch (Exception e) {
            LOGGER.error("Failed to fire OSH scan collected event for job ID {}: {}", job.getId(), e.getMessage(), e);
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
            oshScanFailedEvent.fire(new OshScanFailedEvent(oshScanId, failureReason, retryAttempts));
        } catch (Exception e) {
            LOGGER.error("Failed to fire OSH scan failed event for scan ID {}: {}", oshScanId, e.getMessage(), e);
        }
    }
}
