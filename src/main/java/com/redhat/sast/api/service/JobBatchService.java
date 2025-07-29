package com.redhat.sast.api.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.util.input.CsvConverter;
import com.redhat.sast.api.util.input.CsvJobParser;
import com.redhat.sast.api.v1.dto.request.JobBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;

import io.quarkus.panache.common.Page;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class JobBatchService {

    private static final Logger LOG = Logger.getLogger(JobBatchService.class);

    @Inject
    JobBatchRepository jobBatchRepository;

    @Inject
    JobService jobService;

    @Inject
    PlatformService platformService;

    @Inject
    CsvJobParser csvJobParser;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    GoogleSheetsService googleSheetsService;

    @Inject
    CsvConverter csvConverter;

    /**
     * Submits a batch job for processing.
     */
    public JobBatchResponseDto submitBatch(JobBatchSubmissionDto submissionDto) {
        JobBatch batch = createInitialBatch(submissionDto);

        JobBatchResponseDto response = convertToResponseDto(batch);

        // Start async processing
        managedExecutor.execute(() -> executeBatchProcessing(
                batch.getId(), batch.getBatchGoogleSheetUrl(), batch.getUseKnownFalsePositiveFile()));

        return response;
    }

    @Transactional
    public JobBatch createInitialBatch(JobBatchSubmissionDto submissionDto) {
        JobBatch batch = new JobBatch();
        batch.setBatchGoogleSheetUrl(submissionDto.getBatchGoogleSheetUrl());
        batch.setSubmittedBy(submissionDto.getSubmittedBy());
        batch.setUseKnownFalsePositiveFile(submissionDto.getUseKnownFalsePositiveFile());
        batch.setStatus(BatchStatus.PROCESSING);

        jobBatchRepository.persist(batch);
        LOG.infof(
                "Created new job batch with ID: %d for URL: %s", batch.getId(), submissionDto.getBatchGoogleSheetUrl());

        return batch;
    }

    /**
     * Asynchronously processes a batch by parsing input files and creating individual jobs
     */
    public void executeBatchProcessing(
            @Nonnull Long batchId, @Nonnull String batchGoogleSheetUrl, Boolean useKnownFalsePositiveFile) {
        try {
            LOG.debugf("Starting async processing for batch ID: %d", batchId);
            List<JobCreationDto> jobDtos = fetchAndParseJobsFromSheet(batchGoogleSheetUrl, useKnownFalsePositiveFile);

            if (jobDtos.isEmpty()) {
                updateBatchStatusInNewTransaction(batchId, BatchStatus.COMPLETED_EMPTY, 0, 0, 0);
                return;
            }

            updateBatchTotalJobs(batchId, jobDtos.size());
            LOG.debug(String.format(
                    "Batch %d: Found %d jobs to process. Starting sequential processing.", batchId, jobDtos.size()));

            processJobs(batchId, jobDtos);

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process batch %d: %s", batchId, e.getMessage());
            updateBatchStatusInNewTransaction(batchId, BatchStatus.FAILED, 0, 0, 0);
        }
    }

    /**
     * Extracts the data fetching and parsing logic into a dedicated method.
     * Uses Google Service Account authentication exclusively - no fallback to CSV export.
     */
    private List<JobCreationDto> fetchAndParseJobsFromSheet(String googleSheetUrl) throws Exception {
        // Check if Google Service Account is available
        if (!googleSheetsService.isServiceAccountAvailable()) {
            LOG.errorf("Google Service Account authentication is not available for sheet: %s", googleSheetUrl);
            throw new Exception(
                    "Google Service Account authentication is required but not available. Please check service account configuration.");
        }

        LOG.debugf("Using Google Service Account authentication for sheet: %s", googleSheetUrl);
        try {
            String processedInputContent = csvConverter.convert(googleSheetsService.readSheetData(googleSheetUrl));
            return csvJobParser.parse(processedInputContent);
        } catch (IOException e) {
            LOG.errorf("Google Sheets operation failed: %s", e.getMessage());
            throw new Exception("Google Sheets operation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.errorf("Failed to read Google Sheet: %s", e.getMessage());
            throw new Exception("Failed to read Google Sheet: " + e.getMessage(), e);
        }
    }

    /**
     * Processes a list of jobs sequentially.
     */
    private void processJobs(Long batchId, List<JobCreationDto> jobDtos) {
        final AtomicInteger completedCount = new AtomicInteger(0);
        final AtomicInteger failedCount = new AtomicInteger(0);

        for (JobCreationDto jobDto : jobDtos) {
            try {
                Job createdJob = createJobInNewTransaction(jobDto);
                Long jobId = createdJob.getId();

                associateJobToBatchInNewTransaction(jobId, batchId);

                managedExecutor.execute(() -> platformService.startSastAIWorkflow(createdJob));
                completedCount.incrementAndGet();

            } catch (Exception e) {
                failedCount.incrementAndGet();
                LOG.errorf(e, "Failed during processing of a single job for batch %d", batchId);
            } finally {
                updateBatchProgressInNewTransaction(batchId, completedCount.get(), failedCount.get());
            }
        }
        finalizeBatch(batchId, jobDtos.size(), completedCount.get(), failedCount.get());
    }

    /**
     * Creates a Job in a new, isolated transaction.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public Job createJobInNewTransaction(JobCreationDto jobDto) {
        return jobService.createJobInDatabase(jobDto);
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
     */
    private void finalizeBatch(Long batchId, int total, int completed, int failed) {
        BatchStatus finalStatus = (failed == total)
                ? BatchStatus.FAILED
                : (failed > 0) ? BatchStatus.COMPLETED_WITH_ERRORS : BatchStatus.COMPLETED;
        updateBatchStatusInNewTransaction(batchId, finalStatus, total, completed, failed);
        LOG.infof(
                "Batch %d processing finalized. Status: %s, Total: %d, Completed: %d, Failed: %d",
                batchId, finalStatus, total, completed, failed);
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
                LOG.infof("Updated batch %d progress: completed=%d, failed=%d", batchId, completedJobs, failedJobs);
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to update batch progress for batch %d", batchId);
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
                LOG.infof("Updated batch %d final status: %s", batchId, status);
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to update batch status for batch %d", batchId);
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
        }
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
