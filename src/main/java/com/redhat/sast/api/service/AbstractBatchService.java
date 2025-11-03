package com.redhat.sast.api.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.platform.PipelineParameterMapper;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;

import io.fabric8.tekton.v1.Param;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for batch processing services.
 * Contains common functionality shared between SAST AI and MLOps batch processing.
 */
@Slf4j
public abstract class AbstractBatchService {

    protected final JobBatchRepository jobBatchRepository;
    protected final JobService jobService;
    protected final PlatformService platformService;
    protected final PipelineParameterMapper parameterMapper;
    protected final BatchProcessingService batchProcessingService;

    protected AbstractBatchService(
            JobBatchRepository jobBatchRepository,
            JobService jobService,
            PlatformService platformService,
            PipelineParameterMapper parameterMapper,
            BatchProcessingService batchProcessingService) {
        this.jobBatchRepository = jobBatchRepository;
        this.jobService = jobService;
        this.platformService = platformService;
        this.parameterMapper = parameterMapper;
        this.batchProcessingService = batchProcessingService;
    }

    /**
     * Retrieves a batch by its ID.
     */
    public JobBatchResponseDto getBatchById(@Nonnull Long batchId) {
        JobBatch batch = jobBatchRepository.findById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("Batch not found with id: " + batchId);
        }
        return convertToResponseDto(batch);
    }

    /**
     * Cancels all cancellable jobs in a batch.
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

    /**
     * Determines if a job can be cancelled based on its status.
     */
    protected boolean canBeCancelled(JobStatus status) {
        return status == JobStatus.RUNNING || status == JobStatus.SCHEDULED || status == JobStatus.PENDING;
    }

    /**
     * Converts a JobBatch entity to a response DTO.
     */
    protected JobBatchResponseDto convertToResponseDto(@Nonnull JobBatch batch) {
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

    /**
     * Core job processing loop used by both SAST AI and MLOps pipelines.
     * Template method that delegates pipeline-specific operations to abstract methods.
     */
    protected <T> void processJobsTemplate(
            Long batchId,
            List<T> jobDtos,
            String pipelineName,
            JobProcessor<T> processor) {
        final AtomicInteger completedCount = new AtomicInteger(0);
        final AtomicInteger failedCount = new AtomicInteger(0);

        LOGGER.debug("{} Batch {}: Starting sequential processing of {} jobs", pipelineName, batchId, jobDtos.size());

        for (int i = 0; i < jobDtos.size(); i++) {
            T jobDto = jobDtos.get(i);
            Long jobId = null;

            try {
                String packageNvr = processor.getPackageNvr(jobDto);
                LOGGER.debug(
                        "{} Batch {}: Processing job {}/{} (Package: {})",
                        pipelineName,
                        batchId,
                        i + 1,
                        jobDtos.size(),
                        packageNvr);

                final Job createdJob = processor.createJob(jobDto);
                jobId = createdJob.getId();
                final List<Param> pipelineParams = processor.extractPipelineParams(createdJob);
                final String llmSecretName = (createdJob.getJobSettings() != null)
                        ? createdJob.getJobSettings().getSecretName()
                        : "sast-ai-default-llm-creds";

                batchProcessingService.associateJobToBatchInNewTransaction(jobId, batchId);

                LOGGER.debug(
                        "{} Batch {}: Starting pipeline for job {} (Package: {})",
                        pipelineName,
                        batchId,
                        jobId,
                        packageNvr);

                platformService.startPipeline(jobId, processor.getPipelineName(), pipelineParams, llmSecretName);

                boolean jobCompleted = batchProcessingService.waitForJobCompletion(jobId);

                if (jobCompleted) {
                    completedCount.incrementAndGet();
                    LOGGER.debug(
                            "{} Batch {}: Job {} completed successfully ({}/{} total)",
                            pipelineName,
                            batchId,
                            jobId,
                            completedCount.get(),
                            jobDtos.size());
                } else {
                    failedCount.incrementAndGet();
                    LOGGER.warn(
                            "{} Batch {}: Job {} failed or timed out ({}/{} failed)",
                            pipelineName,
                            batchId,
                            jobId,
                            failedCount.get(),
                            jobDtos.size());
                }

            } catch (Exception e) {
                failedCount.incrementAndGet();
                LOGGER.error(
                        "{} Batch {}: Failed during processing of job {} for package {}",
                        pipelineName,
                        batchId,
                        jobId,
                        processor.getPackageNvr(jobDto),
                        e);
            } finally {
                batchProcessingService.updateBatchProgressInNewTransaction(
                        batchId, completedCount.get(), failedCount.get());
            }
        }

        LOGGER.info(
                "{} Batch {}: Sequential processing completed. Total: {}, Completed: {}, Failed: {}",
                pipelineName,
                batchId,
                jobDtos.size(),
                completedCount.get(),
                failedCount.get());
        batchProcessingService.finalizeBatch(batchId, jobDtos.size(), completedCount.get(), failedCount.get());
    }

    /**
     * Interface for processing jobs in a pipeline-specific manner.
     */
    protected interface JobProcessor<T> {
        String getPackageNvr(T jobDto);

        Job createJob(T jobDto) throws Exception;

        List<Param> extractPipelineParams(Job job);

        String getPipelineName();
    }
}
