package com.redhat.sast.api.service;

import java.io.IOException;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.util.input.CsvConverter;
import com.redhat.sast.api.util.input.CsvJobParser;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Shared service for common batch processing operations.
 * Contains logic that is reused across different pipeline types.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class BatchProcessingService {

    private final JobBatchRepository jobBatchRepository;
    private final JobService jobService;
    private final GoogleSheetsService googleSheetsService;
    private final CsvConverter csvConverter;
    private final CsvJobParser csvJobParser;

    @ConfigProperty(name = "sast.ai.batch.job.polling.interval", defaultValue = "5000")
    long jobPollingIntervalMs;

    @ConfigProperty(name = "sast.ai.batch.job.timeout", defaultValue = "3600000")
    long jobTimeoutMs;

    /**
     * Fetches and parses jobs from a Google Sheet.
     *
     * @param googleSheetUrl the Google Sheet URL
     * @param useKnownFalsePositiveFile whether to use known false positives
     * @return list of job creation DTOs
     * @throws Exception if fetching or parsing fails
     */
    public List<JobCreationDto> fetchAndParseJobsFromSheet(String googleSheetUrl, Boolean useKnownFalsePositiveFile)
            throws Exception {
        // Check if Google Service Account is available
        if (!googleSheetsService.isServiceAccountAvailable()) {
            LOGGER.error("Google Service Account authentication is not available for sheet: {}", googleSheetUrl);
            throw new Exception(
                    "Google Service Account authentication is required but not available. Please check service account configuration.");
        }

        LOGGER.debug("Using Google Service Account authentication for sheet: {}", googleSheetUrl);
        try {
            String processedInputContent = csvConverter.convert(googleSheetsService.readSheetData(googleSheetUrl));
            return csvJobParser.parse(processedInputContent, useKnownFalsePositiveFile);
        } catch (IOException e) {
            LOGGER.error("Google Sheets operation failed: {}", e.getMessage());
            throw new Exception("Google Sheets operation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Failed to read Google Sheet: {}", e.getMessage());
            throw new Exception("Failed to read Google Sheet: " + e.getMessage(), e);
        }
    }

    /**
     * Waits for a job to complete by polling its status.
     *
     * @param jobId the ID of the job to wait for
     * @return true if job completed successfully, false if failed/cancelled/timeout
     */
    public boolean waitForJobCompletion(@Nonnull Long jobId) {
        long startTime = System.currentTimeMillis();
        long timeoutTime = startTime + jobTimeoutMs;

        while (System.currentTimeMillis() < timeoutTime) {
            try {
                JobStatus status = getJobStatusInNewTransaction(jobId);
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
     * Checks the current status of a job in a new transaction.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public JobStatus getJobStatusInNewTransaction(@Nonnull Long jobId) {
        Job job = jobService.getJobEntityById(jobId);
        if (job == null) {
            LOGGER.warn("Job {} not found during status check", jobId);
            return null;
        }
        return job.getStatus();
    }

    /**
     * Associates a Job to a JobBatch in its own isolated transaction.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void associateJobToBatchInNewTransaction(Long jobId, Long batchId) {
        Job job = jobService.getJobEntityById(jobId);
        JobBatch batch = jobBatchRepository.findById(batchId);

        if (job != null && batch != null) {
            job.setJobBatch(batch);
            jobService.persistJob(job);
        }
    }

    /**
     * Sets the final status of the batch once all jobs have been processed.
     */
    public void finalizeBatch(Long batchId, int total, int completed, int failed) {
        BatchStatus finalStatus = (failed == total)
                ? BatchStatus.FAILED
                : (failed > 0) ? BatchStatus.COMPLETED_WITH_ERRORS : BatchStatus.COMPLETED;
        updateBatchStatusInNewTransaction(batchId, finalStatus, total, completed, failed);
        LOGGER.info(
                "Batch {} processing finalized. Status: {}, Total: {}, Completed: {}, Failed: {}",
                batchId,
                finalStatus,
                total,
                completed,
                failed);
    }

    /**
     * Updates batch progress in a new transaction.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateBatchProgressInNewTransaction(@Nonnull Long batchId, int completedJobs, int failedJobs) {
        try {
            JobBatch batch = jobBatchRepository.findById(batchId);
            if (batch != null) {
                batch.setCompletedJobs(completedJobs);
                batch.setFailedJobs(failedJobs);
                jobBatchRepository.persist(batch);
                LOGGER.info("Updated batch {} progress: completed={}, failed={}", batchId, completedJobs, failedJobs);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update batch progress for batch {}", batchId, e);
        }
    }

    /**
     * Updates batch status in a new transaction to handle errors properly.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateBatchStatusInNewTransaction(
            @Nonnull Long batchId, @Nonnull BatchStatus status, int totalJobs, int completedJobs, int failedJobs) {
        try {
            JobBatch batch = jobBatchRepository.findById(batchId);
            if (batch != null) {
                batch.setStatus(status);
                batch.setTotalJobs(totalJobs);
                batch.setCompletedJobs(completedJobs);
                batch.setFailedJobs(failedJobs);
                jobBatchRepository.persist(batch);
                LOGGER.info("Updated batch {} final status: {}", batchId, status);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update batch status for batch {}", batchId, e);
        }
    }

    /**
     * Updates batch total jobs count.
     */
    @Transactional
    public void updateBatchTotalJobs(@Nonnull Long batchId, int totalJobs) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setTotalJobs(totalJobs);
            jobBatchRepository.persist(batch);
        }
    }
}

