package com.redhat.sast.api.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.time.Duration;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.util.input.CsvJobParser;
import com.redhat.sast.api.util.input.InputSourceResolver;
import com.redhat.sast.api.util.input.RemoteContentFetcher;
import com.redhat.sast.api.v1.dto.request.JobBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
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
    InputSourceResolver inputSourceResolver;

    @Inject
    RemoteContentFetcher remoteContentFetcher;

    @Inject
    CsvJobParser csvJobParser;

    @Inject
    ManagedExecutor managedExecutor;

    /**
     * Submits a batch job for processing.
     */
    public JobBatchResponseDto submitBatch(JobBatchSubmissionDto submissionDto) {
        JobBatch batch = createInitialBatch(submissionDto);

        JobBatchResponseDto response = convertToResponseDto(batch);

        // Start async processing
        managedExecutor.execute(() -> executeBatchProcessing(batch.getId(), submissionDto.getBatchGoogleSheetUrl()));

        return response;
    }

    @Transactional
    public JobBatch createInitialBatch(JobBatchSubmissionDto submissionDto) {
        JobBatch batch = new JobBatch();
        batch.setBatchGoogleSheetUrl(submissionDto.getBatchGoogleSheetUrl());
        batch.setSubmittedBy(submissionDto.getSubmittedBy());
        batch.setStatus(BatchStatus.PROCESSING);

        jobBatchRepository.persist(batch);
        LOG.infof(
                "Created new job batch with ID: %d for URL: %s", batch.getId(), submissionDto.getBatchGoogleSheetUrl());

        return batch;
    }

    /**
     * Asynchronously processes a batch by parsing input files and creating individual jobs
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void executeBatchProcessing(@Nonnull Long batchId, @Nonnull String batchGoogleSheetUrl) {
        try {
            LOG.infof("Starting async processing for batch ID: %d", batchId);
            List<JobCreationDto> jobDtos = fetchAndParseJobsFromSheet(batchGoogleSheetUrl);
    
            if (jobDtos.isEmpty()) {
                updateBatchStatusInNewTransaction(batchId, BatchStatus.COMPLETED_EMPTY, 0, 0, 0);
                return;
            }
    
            updateBatchTotalJobs(batchId, jobDtos.size());
            LOG.infof("Batch %d: Found %d jobs to process. Scheduling reactively.", batchId, jobDtos.size());
    
            processJobsReactively(batchId, jobDtos);
    
        } catch (Exception e) {
            LOG.errorf(e, "Failed to process batch %d: %s", batchId, e.getMessage());
            updateBatchStatusInNewTransaction(batchId, BatchStatus.FAILED, 0, 0, 0);
        }
    }
    
    /**
     * Extracts the data fetching and parsing logic into a dedicated method.
     */
    private List<JobCreationDto> fetchAndParseJobsFromSheet(String googleSheetUrl) throws Exception {
        String processedInputUrl = inputSourceResolver.resolve(googleSheetUrl);
        String processedInputContent = remoteContentFetcher.fetch(processedInputUrl);
        return csvJobParser.parse(processedInputContent);
    }
    
    /**
     * Manages the entire reactive pipeline for processing a list of jobs.
     */
    private void processJobsReactively(Long batchId, List<JobCreationDto> jobDtos) {
        final AtomicInteger completedCount = new AtomicInteger(0);
        final AtomicInteger failedCount = new AtomicInteger(0);
        final AtomicInteger processedCount = new AtomicInteger(0);
    
        Multi.createFrom().iterable(jobDtos)
            .onItem().call(jobDto -> Uni.createFrom().nullItem().onItem().delayIt().by(Duration.ofSeconds(1)))
            .subscribe().with(
                jobDto -> handleSingleJob(jobDto, batchId, processedCount, completedCount, failedCount, jobDtos.size()),
                failure -> {
                    LOG.errorf(failure, "A critical error occurred in the batch processing stream for batch %d", batchId);
                    finalizeBatch(batchId, jobDtos.size(), completedCount.get(), failedCount.get());
                },
                () -> {
                    LOG.info("Reactive stream completed for batch " + batchId);
                    finalizeBatch(batchId, jobDtos.size(), completedCount.get(), failedCount.get());
                }
            );
    }
    
    /**
     * Contains the logic for processing one single job from the stream.
     */
    private void handleSingleJob(JobCreationDto jobDto, Long batchId, AtomicInteger processedCount, AtomicInteger completedCount, AtomicInteger failedCount, int totalJobs) {
        try {
            LOG.infof("Processing job %d/%d for batch %d", processedCount.get() + 1, totalJobs, batchId);
            Job createdJob = jobService.createJobInDatabase(jobDto);
            setJobBatchInNewTransaction(createdJob.getId(), batchId);
            managedExecutor.execute(() -> jobService.getPlatformService().startSastAIWorkflow(createdJob));
            completedCount.incrementAndGet();
        } catch (Exception e) {
            failedCount.incrementAndGet();
            LOG.errorf(e, "Failed to create job %d/%d for batch %d", processedCount.get() + 1, totalJobs, batchId);
        } finally {
            processedCount.incrementAndGet();
            updateBatchProgressInNewTransaction(batchId, completedCount.get(), failedCount.get());
        }
    }
    /**
     * Sets the final status of the batch once all jobs have been processed.
     */
    private void finalizeBatch(Long batchId, int total, int completed, int failed) {
        BatchStatus finalStatus = (failed == total) ? BatchStatus.FAILED
                : (failed > 0) ? BatchStatus.COMPLETED_WITH_ERRORS
                : BatchStatus.COMPLETED;
        updateBatchStatusInNewTransaction(batchId, finalStatus, total, completed, failed);
        LOG.infof("Batch %d processing finalized. Status: %s, Total: %d, Completed: %d, Failed: %d",
                batchId, finalStatus, total, completed, failed);
    }

    /**
     * Sets job batch relationship in a new transaction (to avoid context issues)
     */
    @Transactional
    public void setJobBatchInNewTransaction(@Nonnull Long jobId, @Nonnull Long batchId) {
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
