package com.redhat.sast.api.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.v1.dto.request.JobBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;

import io.quarkus.panache.common.Page;
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
    BatchInputService batchInputService;

    @Inject
    ManagedExecutor managedExecutor;

    /**
     * Submits a batch job for processing.
     */
    public JobBatchResponseDto submitBatch(JobBatchSubmissionDto submissionDto) {
        JobBatch batch = createInitialBatch(submissionDto);

        JobBatchResponseDto response = convertToResponseDto(batch);

        startAsyncProcessing(batch.getId(), submissionDto.getSourceUrl());

        return response;
    }

    @Transactional
    public JobBatch createInitialBatch(JobBatchSubmissionDto submissionDto) {
        JobBatch batch = new JobBatch();
        batch.setSourceUrl(submissionDto.getSourceUrl());
        batch.setSubmittedBy(submissionDto.getSubmittedBy());
        batch.setStatus("PROCESSING");

        jobBatchRepository.persist(batch);
        LOG.infof("Created new job batch with ID: %d for URL: %s", batch.getId(), submissionDto.getSourceUrl());

        return batch;
    }

    private void startAsyncProcessing(Long batchId, String sheetsUrl) {
        // Start async processing
        managedExecutor.execute(() -> processBatchAsync(batchId, sheetsUrl));
    }

    /**
     * Asynchronously processes a batch by parsing input files and creating individual jobs
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void processBatchAsync(Long batchId, String sheetsUrl) {
        try {
            LOG.infof("Starting async processing for batch ID: %d", batchId);

            String processedInputUrl = batchInputService.processInputSource(sheetsUrl);

            String processedInputContent = batchInputService.fetchInputData(processedInputUrl);

            List<JobCreationDto> jobDtos = batchInputService.parseInputToJobs(processedInputContent);

            if (jobDtos.isEmpty()) {
                updateBatchStatusInNewTransaction(batchId, "COMPLETED_EMPTY", 0, 0, 0);
                LOG.warnf("Batch %d completed with no valid jobs found in the Google Sheet", batchId);
                return;
            }

            updateBatchTotalJobs(batchId, jobDtos.size());
            LOG.infof("Batch %d: Found %d jobs to process", batchId, jobDtos.size());

            processJobsSequentially(batchId, jobDtos);

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process batch %d: %s", batchId, e.getMessage());
            updateBatchStatusInNewTransaction(batchId, "FAILED", 0, 0, 0);
        }
    }

    /**
     * Processes jobs sequentially, updating progress after each job
     */
    private void processJobsSequentially(Long batchId, List<JobCreationDto> jobDtos) {
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);

        for (int i = 0; i < jobDtos.size(); i++) {
            JobCreationDto jobDto = jobDtos.get(i);

            try {
                LOG.infof("Processing job %d/%d for batch %d", i + 1, jobDtos.size(), batchId);

                // Create individual job in database and start pipeline
                Job createdJob = jobService.createJobInDatabase(jobDto);

                setJobBatchInNewTransaction(createdJob.getId(), batchId);

                // Start pipeline execution asynchronously
                managedExecutor.execute(() -> {
                    try {
                        jobService.getPlatformService().startSastAIWorkflow(createdJob);
                        LOG.infof("Successfully started pipeline for job %d in batch %d", createdJob.getId(), batchId);
                    } catch (Exception e) {
                        LOG.errorf(e, "Failed to start pipeline for job %d in batch %d", createdJob.getId(), batchId);
                    }
                });

                completedCount.incrementAndGet();
                LOG.infof("Successfully created job %d (ID: %d) for batch %d", i + 1, createdJob.getId(), batchId);

            } catch (Exception e) {
                failedCount.incrementAndGet();
                LOG.errorf(
                        e,
                        "Failed to create job %d/%d for batch %d: %s",
                        i + 1,
                        jobDtos.size(),
                        batchId,
                        e.getMessage());
            }

            updateBatchProgressInNewTransaction(batchId, completedCount.get(), failedCount.get());

            try {
                Thread.sleep(1000); // 1 second delay between job creations
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.warnf("Batch processing interrupted for batch %d", batchId);
                break;
            }
        }

        // Final status update
        String finalStatus = (failedCount.get() == jobDtos.size())
                ? "FAILED"
                : (failedCount.get() > 0) ? "COMPLETED_WITH_ERRORS" : "COMPLETED";

        updateBatchStatusInNewTransaction(
                batchId, finalStatus, jobDtos.size(), completedCount.get(), failedCount.get());
        LOG.infof(
                "Batch %d processing completed. Status: %s, Total: %d, Completed: %d, Failed: %d",
                batchId, finalStatus, jobDtos.size(), completedCount.get(), failedCount.get());
    }

    /**
     * Sets job batch relationship in a new transaction to avoid context issues
     */
    @Transactional
    public void setJobBatchInNewTransaction(Long jobId, Long batchId) {
        try {
            Job job = jobService.getJobEntityById(jobId);
            JobBatch batch = jobBatchRepository.findById(batchId);
            if (job != null && batch != null) {
                job.setJobBatch(batch);
                jobService.persistJob(job);
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to set batch relationship for job %d and batch %d", jobId, batchId);
        }
    }

    /**
     * Updates batch progress in a new transaction
     */
    @Transactional
    public void updateBatchProgressInNewTransaction(Long batchId, int completedJobs, int failedJobs) {
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
            Long batchId, String status, int totalJobs, int completedJobs, int failedJobs) {
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
    public void updateBatchTotalJobs(Long batchId, int totalJobs) {
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

    public JobBatchResponseDto getBatchById(Long batchId) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("Batch not found with id: " + batchId);
        }
        return convertToResponseDto(batch);
    }

    @Transactional
    public void updateBatchStatus(Long batchId, String status) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setStatus(status);
            jobBatchRepository.persist(batch);
        }
    }

    @Transactional
    public void updateBatchStatus(Long batchId, String status, int totalJobs, int completedJobs, int failedJobs) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setStatus(status);
            batch.setTotalJobs(totalJobs);
            batch.setCompletedJobs(completedJobs);
            batch.setFailedJobs(failedJobs);
            jobBatchRepository.persist(batch);
        }
    }

    private JobBatchResponseDto convertToResponseDto(JobBatch batch) {
        JobBatchResponseDto dto = new JobBatchResponseDto();
        dto.setBatchId(batch.getId());
        dto.setSourceUrl(batch.getSourceUrl());
        dto.setSubmittedBy(batch.getSubmittedBy());
        dto.setSubmittedAt(batch.getSubmittedAt());
        dto.setStatus(batch.getStatus());
        dto.setTotalJobs(batch.getTotalJobs());
        dto.setCompletedJobs(batch.getCompletedJobs());
        dto.setFailedJobs(batch.getFailedJobs());
        return dto;
    }
}
