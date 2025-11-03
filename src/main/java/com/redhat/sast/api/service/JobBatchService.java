package com.redhat.sast.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.redhat.sast.api.common.constants.PipelineConstants;
import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.platform.PipelineParameterMapper;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.service.BatchProcessingService;
import com.redhat.sast.api.v1.dto.request.JobBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;

import io.fabric8.tekton.v1.Param;
import io.quarkus.panache.common.Page;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing SAST AI batch jobs.
 * Handles submission and processing of SAST AI pipeline batches.
 */
@ApplicationScoped
@Slf4j
public class JobBatchService extends AbstractBatchService {

    private final ManagedExecutor managedExecutor;

    // No-args constructor required by Quarkus CDI for proxying
    protected JobBatchService() {
        super(null, null, null, null, null);
        this.managedExecutor = null;
    }

    public JobBatchService(
            JobBatchRepository jobBatchRepository,
            JobService jobService,
            PlatformService platformService,
            PipelineParameterMapper parameterMapper,
            BatchProcessingService batchProcessingService,
            ManagedExecutor managedExecutor) {
        super(jobBatchRepository, jobService, platformService, parameterMapper, batchProcessingService);
        this.managedExecutor = managedExecutor;
    }

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
                batch.getSubmittedBy()));

        return response;
    }

    @Transactional
    public JobBatch createInitialBatch(JobBatchSubmissionDto submissionDto) {
        JobBatch batch = new JobBatch();
        batch.setBatchGoogleSheetUrl(submissionDto.getBatchGoogleSheetUrl());
        batch.setSubmittedBy(submissionDto.getSubmittedBy() != null ? submissionDto.getSubmittedBy() : "unknown");
        batch.setUseKnownFalsePositiveFile(submissionDto.getUseKnownFalsePositiveFile());
        batch.setStatus(BatchStatus.PROCESSING);
        batch.setPipelineName(PipelineConstants.SAST_AI_PIPELINE);

        jobBatchRepository.persist(batch);
        LOGGER.info(
                "Created new job batch with ID: {} for URL: {}", batch.getId(), submissionDto.getBatchGoogleSheetUrl());

        return batch;
    }

    /**
     * Asynchronously processes a batch by parsing input files and creating individual jobs.
     */
    public void executeBatchProcessing(
            @Nonnull Long batchId,
            @Nonnull String batchGoogleSheetUrl,
            Boolean useKnownFalsePositiveFile,
            String submittedBy) {
        try {
            List<JobCreationDto> jobDtos =
                    batchProcessingService.fetchAndParseJobsFromSheet(batchGoogleSheetUrl, useKnownFalsePositiveFile);

            if (jobDtos.isEmpty()) {
                batchProcessingService.updateBatchStatusInNewTransaction(batchId, BatchStatus.COMPLETED_EMPTY, 0, 0, 0);
                return;
            }

            jobDtos.forEach(dto -> dto.setSubmittedBy(submittedBy));

            batchProcessingService.updateBatchTotalJobs(batchId, jobDtos.size());
            LOGGER.debug(
                    "Batch {}: Found {} jobs to process. Starting sequential processing.", batchId, jobDtos.size());

            processJobs(batchId, jobDtos);

        } catch (Exception e) {
            LOGGER.error("Failed to process batch {}: {}", batchId, e.getMessage(), e);
            batchProcessingService.updateBatchStatusInNewTransaction(batchId, BatchStatus.FAILED, 0, 0, 0);
        }
    }

    /**
     * Processes a list of jobs sequentially.
     */
    private void processJobs(Long batchId, List<JobCreationDto> jobDtos) {
        processJobsTemplate(batchId, jobDtos, "SAST AI", new JobProcessor<JobCreationDto>() {
            @Override
            public String getPackageNvr(JobCreationDto jobDto) {
                return jobDto.getPackageNvr();
            }

            @Override
            public Job createJob(JobCreationDto jobDto) throws Exception {
                return createJobInNewTransaction(jobDto);
            }

            @Override
            public List<Param> extractPipelineParams(Job job) {
                return parameterMapper.extractPipelineParams(job);
            }

            @Override
            public String getPipelineName() {
                return PipelineConstants.SAST_AI_PIPELINE;
            }
        });
    }

    /**
     * Creates a Job in a new, isolated transaction.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public Job createJobInNewTransaction(JobCreationDto jobDto) {
        return jobService.createJobEntity(jobDto);
    }

    /**
     * Updates job status in a new transaction.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateJobStatusInNewTransaction(Long jobId, JobStatus status) {
        jobService.updateJobStatus(jobId, status);
    }

    public List<JobBatchResponseDto> getAllBatches(int page, int size) {
        return jobBatchRepository
                .find("pipelineName", PipelineConstants.SAST_AI_PIPELINE)
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
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
}
