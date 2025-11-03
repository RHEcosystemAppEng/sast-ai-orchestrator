package com.redhat.sast.api.service;

import java.util.List;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.redhat.sast.api.common.constants.PipelineConstants;
import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.platform.PipelineParameterMapper;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.v1.dto.request.MlOpsJobCreationDto;
import com.redhat.sast.api.v1.dto.request.MlOpsJobBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.response.JobBatchResponseDto;

import io.fabric8.tekton.v1.Param;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing MLOps batch jobs.
 * Handles submission and processing of MLOps pipeline batches.
 */
@ApplicationScoped
@Slf4j
public class MlOpsBatchService extends AbstractBatchService {

    private final ManagedExecutor managedExecutor;
    private final PackageListResolver packageListResolver;

    // No-args constructor required by Quarkus CDI for proxying
    protected MlOpsBatchService() {
        super(null, null, null, null, null);
        this.managedExecutor = null;
        this.packageListResolver = null;
    }

    public MlOpsBatchService(
            JobBatchRepository jobBatchRepository,
            JobService jobService,
            PlatformService platformService,
            PipelineParameterMapper parameterMapper,
            BatchProcessingService batchProcessingService,
            ManagedExecutor managedExecutor,
            PackageListResolver packageListResolver) {
        super(jobBatchRepository, jobService, platformService, parameterMapper, batchProcessingService);
        this.managedExecutor = managedExecutor;
        this.packageListResolver = packageListResolver;
    }

    /**
     * Submits an MLOps batch job for processing.
     */
    public JobBatchResponseDto submitMlOpsBatch(MlOpsJobBatchSubmissionDto submissionDto) {
        JobBatch batch = createInitialMlOpsBatch(submissionDto);

        JobBatchResponseDto response = convertToResponseDto(batch);

        managedExecutor.execute(() -> executeMlOpsBatchProcessing(
                batch.getId(),
                batch.getUseKnownFalsePositiveFile(),
                batch.getSubmittedBy(),
                submissionDto.getDvcNvrVersion(),
                submissionDto.getDvcKnownFalsePositivesVersion(),
                submissionDto.getDvcPromptsVersion(),
                submissionDto.getImageVersion()));

        return response;
    }

    @Transactional
    public JobBatch createInitialMlOpsBatch(MlOpsJobBatchSubmissionDto submissionDto) {
        JobBatch batch = new JobBatch();
        batch.setSubmittedBy(submissionDto.getSubmittedBy() != null ? submissionDto.getSubmittedBy() : "unknown");
        batch.setUseKnownFalsePositiveFile(submissionDto.getUseKnownFalsePositiveFile());
        batch.setStatus(BatchStatus.PROCESSING);
        batch.setPipelineName(PipelineConstants.MLOPS_PIPELINE);

        jobBatchRepository.persist(batch);
        LOGGER.info("Created new MLOps job batch with ID: {}", batch.getId());

        return batch;
    }

    /**
     * Asynchronously processes an MLOps batch by fetching package list and creating individual jobs.
     */
    public void executeMlOpsBatchProcessing(
            @Nonnull Long batchId,
            Boolean useKnownFalsePositiveFile,
            String submittedBy,
            String dvcNvrVersion,
            String dvcKnownFalsePositivesVersion,
            String dvcPromptsVersion,
            String imageVersion) {
        try {
            // Get package list from resolver (hardcoded for testing, will use DVC in future)
            List<String> packageNvrs = packageListResolver.getPackageListForNvr(dvcNvrVersion);

            if (packageNvrs.isEmpty()) {
                batchProcessingService.updateBatchStatusInNewTransaction(batchId, BatchStatus.COMPLETED_EMPTY, 0, 0, 0);
                return;
            }

            // Convert package NVRs to MlOpsJobCreationDto objects
            List<MlOpsJobCreationDto> jobDtos = packageNvrs.stream()
                    .map(nvr -> new MlOpsJobCreationDto(nvr, useKnownFalsePositiveFile, submittedBy))
                    .toList();

            batchProcessingService.updateBatchTotalJobs(batchId, jobDtos.size());
            LOGGER.debug(
                    "MLOps Batch {}: Found {} packages to process. Starting sequential processing.",
                    batchId,
                    jobDtos.size());

            processMlOpsJobs(
                    batchId,
                    jobDtos,
                    dvcNvrVersion,
                    dvcKnownFalsePositivesVersion,
                    dvcPromptsVersion,
                    imageVersion);

        } catch (Exception e) {
            LOGGER.error("Failed to process MLOps batch {}: {}", batchId, e.getMessage(), e);
            batchProcessingService.updateBatchStatusInNewTransaction(batchId, BatchStatus.FAILED, 0, 0, 0);
        }
    }

    /**
     * Processes a list of MLOps jobs sequentially.
     */
    private void processMlOpsJobs(
            Long batchId,
            List<MlOpsJobCreationDto> jobDtos,
            String dvcNvrVersion,
            String dvcKnownFalsePositivesVersion,
            String dvcPromptsVersion,
            String imageVersion) {
        processJobsTemplate(batchId, jobDtos, "MLOps", new JobProcessor<MlOpsJobCreationDto>() {
            @Override
            public String getPackageNvr(MlOpsJobCreationDto jobDto) {
                return jobDto.getPackageNvr();
            }

            @Override
            public Job createJob(MlOpsJobCreationDto jobDto) throws Exception {
                return createMlOpsJobInNewTransaction(
                        jobDto,
                        dvcNvrVersion,
                        dvcKnownFalsePositivesVersion,
                        dvcPromptsVersion,
                        imageVersion);
            }

            @Override
            public List<Param> extractPipelineParams(Job job) {
                return parameterMapper.extractMlOpsPipelineParams(job);
            }

            @Override
            public String getPipelineName() {
                return PipelineConstants.MLOPS_PIPELINE;
            }
        });
    }

    /**
     * Creates an MLOps Job with MLOps-specific settings in a new, isolated transaction.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public Job createMlOpsJobInNewTransaction(
            MlOpsJobCreationDto jobDto,
            String dvcNvrVersion,
            String dvcKnownFalsePositivesVersion,
            String dvcPromptsVersion,
            String imageVersion) {
        Job job = jobService.createMlOpsJobEntity(
                jobDto,
                dvcNvrVersion,
                dvcKnownFalsePositivesVersion,
                dvcPromptsVersion,
                imageVersion);

        jobService.persistJob(job);
        return job;
    }

    public List<JobBatchResponseDto> getAllBatches(int page, int size) {
        return jobBatchRepository
                .find("pipelineName", PipelineConstants.MLOPS_PIPELINE)
                .page(io.quarkus.panache.common.Page.of(page, size))
                .list()
                .stream()
                .map(this::convertToResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }

}

