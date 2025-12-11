package com.redhat.sast.api.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.redhat.sast.api.common.constants.ApplicationConstants;
import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.exceptions.SastAiConfigException;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.platform.PipelineParameterMapper;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.util.input.CsvConverter;
import com.redhat.sast.api.util.input.CsvJobParser;
import com.redhat.sast.api.v1.dto.request.JobBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;

import io.fabric8.tekton.v1.Param;
import io.quarkus.panache.common.Page;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class JobBatchService {

    private final JobBatchRepository jobBatchRepository;
    private final JobService jobService;
    private final PlatformService platformService;
    private final CsvJobParser csvJobParser;
    private final ManagedExecutor managedExecutor;
    private final GoogleSheetsService googleSheetsService;
    private final CsvConverter csvConverter;
    private final PipelineParameterMapper parameterMapper;
    private final EventBroadcastService eventBroadcastService;
    private final BatchOperationsHelper batchOperationsHelper;

    /**
     * Submits a batch job for processing.
     */
    public JobBatchResponseDto submitBatch(JobBatchSubmissionDto submissionDto) {
        JobBatch batch = createInitialBatch(submissionDto);

        JobBatchResponseDto response = convertToResponseDto(batch);

        managedExecutor.execute(() -> executeBatchProcessing(
                batch.getId(),
                batch.getBatchGoogleSheetUrl(),
                batch.getUseKnownFalsePositiveFile(),
                batch.getSubmittedBy(),
                batch.getAggregateResultsGSheet()));

        return response;
    }

    @Transactional
    public JobBatch createInitialBatch(JobBatchSubmissionDto submissionDto) {
        JobBatch batch = new JobBatch();
        batch.setBatchGoogleSheetUrl(submissionDto.getBatchGoogleSheetUrl());
        // Set submittedBy with default value "unknown" if not provided
        batch.setSubmittedBy(submissionDto.getSubmittedBy() != null ? submissionDto.getSubmittedBy() : "unknown");
        batch.setUseKnownFalsePositiveFile(submissionDto.getUseKnownFalsePositiveFile());
        batch.setAggregateResultsGSheet(submissionDto.getAggregateResultsGSheet());
        batch.setStatus(BatchStatus.PROCESSING);

        jobBatchRepository.persist(batch);
        LOGGER.info(
                "Created new job batch with ID: {} for URL: {}", batch.getId(), submissionDto.getBatchGoogleSheetUrl());

        return batch;
    }

    /**
     * Asynchronously processes a batch by parsing input files and creating individual jobs.
     * Jobs within the batch are processed sequentially to prevent embedding model overload.
     */
    public void executeBatchProcessing(
            @Nonnull Long batchId,
            @Nonnull String batchGoogleSheetUrl,
            Boolean useKnownFalsePositiveFile,
            String submittedBy,
            String aggregateResultsGSheet) {
        try {
            List<JobCreationDto> jobDtos = fetchAndParseJobsFromSheet(batchGoogleSheetUrl, useKnownFalsePositiveFile);

            if (jobDtos.isEmpty()) {
                updateBatchStatusInNewTransaction(batchId, BatchStatus.COMPLETED_EMPTY, 0, 0, 0);
                return;
            }

            jobDtos.forEach(dto -> {
                dto.setSubmittedBy(submittedBy);
                if (ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK.test(aggregateResultsGSheet)) {
                    dto.setAggregateResultsGSheet(aggregateResultsGSheet);
                }
            });

            updateBatchTotalJobs(batchId, jobDtos.size());
            LOGGER.debug(
                    "Batch {}: Found {} jobs to process. Starting sequential processing.", batchId, jobDtos.size());

            processJobs(batchId, jobDtos);

        } catch (SastAiConfigException e) {
            LOGGER.error("Failed to process batch {}: {}", batchId, e.getMessage(), e);
            updateBatchStatusInNewTransaction(batchId, BatchStatus.FAILED, 0, 0, 0);
        }
    }

    /**
     * Extracts the data fetching and parsing logic into a dedicated method.
     * Uses Google Service Account authentication exclusively - no fallback to CSV export.
     */
    private List<JobCreationDto> fetchAndParseJobsFromSheet(String googleSheetUrl, Boolean useKnownFalsePositiveFile) {
        // Check if Google Service Account is available
        if (!googleSheetsService.isServiceAccountAvailable()) {
            LOGGER.error("Google Service Account authentication is not available for sheet: {}", googleSheetUrl);
            throw new SastAiConfigException(
                    "Google Service Account authentication is required but not available. Please check service account configuration.");
        }

        LOGGER.debug("Using Google Service Account authentication for sheet: {}", googleSheetUrl);
        try {
            String processedInputContent = csvConverter.convert(googleSheetsService.readSheetData(googleSheetUrl));
            return csvJobParser.parse(processedInputContent, useKnownFalsePositiveFile);
        } catch (IOException e) {
            LOGGER.error("Google Sheets operation failed: {}", e.getMessage());
            throw new SastAiConfigException("Google Sheets operation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Failed to read Google Sheet: {}", e.getMessage());
            throw new SastAiConfigException("Failed to read Google Sheet: " + e.getMessage(), e);
        }
    }

    /**
     * Checks the current status of a job in a new transaction.
     *
     * @param jobId the ID of the job to check
     * @return the current JobStatus, or null if job not found
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
     * Waits for a job to complete by polling its status.
     * Delegates to BatchOperationsHelper for common polling logic.
     *
     * @param jobId the ID of the job to wait for
     * @return true if job completed successfully, false if failed/cancelled/timeout
     */
    private boolean waitForJobCompletion(@Nonnull Long jobId) {
        return batchOperationsHelper.waitForJobCompletion(jobId, this::getJobStatusInNewTransaction);
    }

    /**
     * Processes a list of jobs sequentially.
     */
    private void processJobs(Long batchId, List<JobCreationDto> jobDtos) {
        final AtomicInteger completedCount = new AtomicInteger(0);
        final AtomicInteger failedCount = new AtomicInteger(0);

        LOGGER.debug("Batch {}: Starting sequential processing of {} jobs", batchId, jobDtos.size());

        for (int i = 0; i < jobDtos.size(); i++) {
            JobCreationDto jobDto = jobDtos.get(i);
            Long jobId = null;

            try {
                LOGGER.debug(
                        "Batch {}: Processing job {}/{} (Package: {})",
                        batchId,
                        i + 1,
                        jobDtos.size(),
                        jobDto.getPackageNvr());

                final Job createdJob = createJobInNewTransaction(jobDto);
                jobId = createdJob.getId();
                final List<Param> pipelineParams = parameterMapper.extractPipelineParams(createdJob);
                final String llmSecretName = (createdJob.getJobSettings() != null)
                        ? createdJob.getJobSettings().getSecretName()
                        : "sast-ai-default-llm-creds";

                associateJobToBatchInNewTransaction(jobId, batchId);

                LOGGER.debug(
                        "Batch {}: Starting pipeline for job {} (Package: {})", batchId, jobId, jobDto.getPackageNvr());

                platformService.startSastAIWorkflow(jobId, pipelineParams, llmSecretName);

                boolean jobCompleted = waitForJobCompletion(jobId);

                if (jobCompleted) {
                    completedCount.incrementAndGet();
                    LOGGER.debug(
                            "Batch {}: Job {} completed successfully ({}/{} total)",
                            batchId,
                            jobId,
                            completedCount.get(),
                            jobDtos.size());
                } else {
                    failedCount.incrementAndGet();
                    LOGGER.warn(
                            "Batch {}: Job {} failed or timed out ({}/{} failed)",
                            batchId,
                            jobId,
                            failedCount.get(),
                            jobDtos.size());
                }

            } catch (Exception e) {
                failedCount.incrementAndGet();
                LOGGER.error(
                        "Batch {}: Failed during processing of job {} for package {}",
                        batchId,
                        jobId,
                        jobDto.getPackageNvr(),
                        e);
            } finally {
                updateBatchProgressInNewTransaction(batchId, completedCount.get(), failedCount.get());
            }
        }

        LOGGER.info(
                "Batch {}: Sequential processing completed. Total: {}, Completed: {}, Failed: {}",
                batchId,
                jobDtos.size(),
                completedCount.get(),
                failedCount.get());
        finalizeBatch(batchId, jobDtos.size(), completedCount.get(), failedCount.get());
    }

    /**
     * Creates a Job in a new, isolated transaction.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public Job createJobInNewTransaction(JobCreationDto jobDto) {
        return jobService.createJobEntity(jobDto);
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
     * Updates job status in a new transaction
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateJobStatusInNewTransaction(Long jobId, JobStatus status) {
        jobService.updateJobStatus(jobId, status);
    }

    /**
     * Sets the final status of the batch once all jobs have been processed.
     * Uses BatchOperationsHelper to determine the appropriate final status.
     */
    private void finalizeBatch(Long batchId, int total, int completed, int failed) {
        BatchStatus finalStatus = batchOperationsHelper.determineFinalBatchStatus(total, completed, failed);
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
     * Updates batch progress in a new transaction
     */
    @Transactional
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
     * Updates batch status in a new transaction to handle errors properly
     */
    @Transactional
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

                eventBroadcastService.broadcastBatchProgress(batch);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update batch status for batch {}", batchId, e);
        }
    }

    @Transactional
    public void updateBatchTotalJobs(@Nonnull Long batchId, int totalJobs) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setTotalJobs(totalJobs);
            jobBatchRepository.persist(batch);
        }
    }

    public List<JobBatchResponseDto> getAllBatches(int page, int size) {
        return jobBatchRepository.findAll().page(Page.of(page, size)).list().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public JobBatchResponseDto getBatchById(@Nonnull Long batchId) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("Batch not found with id: " + batchId);
        }
        return convertToResponseDto(batch);
    }

    @Transactional
    public void updateBatchStatus(@Nonnull Long batchId, @Nonnull BatchStatus status) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setStatus(status);
            jobBatchRepository.persist(batch);
        }
    }

    @Transactional
    public void updateBatchStatus(
            @Nonnull Long batchId, @Nonnull BatchStatus status, int totalJobs, int completedJobs, int failedJobs) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setStatus(status);
            batch.setTotalJobs(totalJobs);
            batch.setCompletedJobs(completedJobs);
            batch.setFailedJobs(failedJobs);
            jobBatchRepository.persist(batch);

            eventBroadcastService.broadcastBatchProgress(batch);
        }
    }

    /**
     * Cancels all cancellable jobs in a batch.
     *
     * @param batchId the ID of the batch to cancel
     * @return the number of jobs that were cancelled
     */
    @Transactional
    public int cancelJobBatch(@Nonnull Long batchId) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("Job batch not found with id: " + batchId);
        }

        List<Job> cancellableJobs = batch.getJobs().stream()
                .filter(job -> canBeCancelled(job.getStatus()))
                .toList();

        if (cancellableJobs.isEmpty()) {
            LOGGER.info("No cancellable jobs found in batch {}", batchId);
            return 0;
        }

        LOGGER.info("Cancelling {} jobs in batch {}", cancellableJobs.size(), batchId);

        int cancelledCount = 0;
        for (Job job : cancellableJobs) {
            try {
                jobService.cancelJob(job.getId());
                cancelledCount++;
            } catch (Exception e) {
                LOGGER.error("Failed to cancel job {} in batch {}", job.getId(), batchId, e);
            }
        }

        batch.setStatus(BatchStatus.CANCELLED);
        jobBatchRepository.persist(batch);

        LOGGER.info("Cancelled {} out of {} jobs in batch {}", cancelledCount, cancellableJobs.size(), batchId);
        return cancelledCount;
    }

    private boolean canBeCancelled(JobStatus status) {
        return status == JobStatus.RUNNING || status == JobStatus.SCHEDULED || status == JobStatus.PENDING;
    }

    private JobBatchResponseDto convertToResponseDto(@Nonnull JobBatch batch) {
        JobBatchResponseDto dto = new JobBatchResponseDto();
        dto.setBatchId(batch.getId());
        dto.setBatchGoogleSheetUrl(batch.getBatchGoogleSheetUrl());
        dto.setSubmittedBy(batch.getSubmittedBy());
        dto.setSubmittedAt(batch.getSubmittedAt());
        dto.setStatus(batch.getStatus());
        dto.setTotalJobs(batch.getTotalJobs());
        dto.setCompletedJobs(batch.getCompletedJobs());
        dto.setFailedJobs(batch.getFailedJobs());
        return dto;
    }
}
