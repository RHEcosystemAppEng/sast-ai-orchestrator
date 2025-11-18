package com.redhat.sast.api.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper service providing common batch operations for both JobBatch and MLOpsBatch.
 * Contains reusable logic for job polling, status checking, and common transaction patterns.
 */
@ApplicationScoped
@Slf4j
public class BatchOperationsHelper {

    @ConfigProperty(name = "sast.ai.batch.job.polling.interval", defaultValue = "5000")
    long jobPollingIntervalMs;

    @ConfigProperty(name = "sast.ai.batch.job.timeout", defaultValue = "3600000")
    long jobTimeoutMs;

    /**
     * Waits for a job to complete by polling its status.
     *
     * @param jobId the ID of the job to wait for
     * @param statusChecker a functional interface to check job status
     * @return true if job completed successfully, false if failed/cancelled/timeout
     */
    public boolean waitForJobCompletion(@Nonnull Long jobId, JobStatusChecker statusChecker) {
        long startTime = System.currentTimeMillis();
        long timeoutTime = startTime + jobTimeoutMs;

        while (System.currentTimeMillis() < timeoutTime) {
            try {
                JobStatus status = statusChecker.checkStatus(jobId);
                if (status == null) {
                    LOGGER.warn("Job {} not found during polling", jobId);
                    return false;
                }

                LOGGER.debug("Polling job {} status: {}", jobId, status);

                switch (status) {
                    case COMPLETED:
                        LOGGER.debug("Job {} completed successfully", jobId);
                        return true;
                    case FAILED, CANCELLED:
                        LOGGER.warn("Job {} finished with status: {}", jobId, status);
                        return false;
                    case PENDING, SCHEDULED, RUNNING:
                        // Job is still processing, continue polling
                        break;
                    default:
                        LOGGER.warn("Job {} has unexpected status: {}", jobId, status);
                        break;
                }

                Thread.sleep(jobPollingIntervalMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Job polling interrupted for job {}", jobId, e);
                return false;
            } catch (Exception e) {
                LOGGER.error("Error polling job {} status", jobId, e);
                return false;
            }
        }

        LOGGER.error("Job {} timed out after {}ms", jobId, jobTimeoutMs);
        return false;
    }

    /**
     * Determines the final batch status based on job completion counts.
     *
     * @param totalJobs total number of jobs in the batch
     * @param completedJobs number of successfully completed jobs
     * @param failedJobs number of failed jobs
     * @return the final BatchStatus
     */
    public BatchStatus determineFinalBatchStatus(int totalJobs, int completedJobs, int failedJobs) {
        if (totalJobs == 0) {
            return BatchStatus.COMPLETED_EMPTY;
        }

        if (completedJobs == totalJobs) {
            return BatchStatus.COMPLETED;
        } else if (completedJobs + failedJobs == totalJobs) {
            return BatchStatus.COMPLETED_WITH_ERRORS;
        } else {
            return BatchStatus.FAILED;
        }
    }

    /**
     * Functional interface for checking job status in a transaction.
     */
    @FunctionalInterface
    public interface JobStatusChecker {
        JobStatus checkStatus(Long jobId);
    }
}
