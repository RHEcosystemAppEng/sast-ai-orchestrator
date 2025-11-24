package com.redhat.sast.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.exceptions.DvcException;
import com.redhat.sast.api.exceptions.InvalidNvrException;
import com.redhat.sast.api.mapper.JobSettingsMapper;
import com.redhat.sast.api.model.MlOpsBatch;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobSettings;
import com.redhat.sast.api.platform.PipelineParameterMapper;
import com.redhat.sast.api.repository.MlOpsBatchRepository;
import com.redhat.sast.api.repository.MlOpsJobRepository;
import com.redhat.sast.api.v1.dto.request.JobSettingsDto;
import com.redhat.sast.api.v1.dto.request.MlOpsBatchSubmissionDto;
import com.redhat.sast.api.v1.dto.response.MlOpsBatchResponseDto;

import io.fabric8.tekton.v1.Param;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MlOpsBatchService {

    private final MlOpsBatchRepository mlOpsBatchRepository;
    private final MlOpsJobRepository mlOpsJobRepository;
    private final DvcService dvcService;
    private final NvrResolutionService nvrResolutionService;
    private final PipelineParameterMapper parameterMapper;
    private final PlatformService platformService;
    private final ManagedExecutor managedExecutor;
    private final BatchOperationsHelper batchOperationsHelper;

    @ConfigProperty(name = "mlops.batch.max.parallel.jobs", defaultValue = "3")
    int maxParallelJobs;

    /**
     * Submits an MLOps batch for processing.
     */
    public MlOpsBatchResponseDto submitBatch(MlOpsBatchSubmissionDto submissionDto) {
        MlOpsBatch batch = createInitialBatch(submissionDto);

        MlOpsBatchResponseDto response = convertToResponseDto(batch);

        // Execute batch processing asynchronously
        managedExecutor.execute(() -> executeBatchProcessing(
                batch.getId(),
                batch.getTestingDataNvrsVersion(),
                batch.getPromptsVersion(),
                batch.getKnownNonIssuesVersion(),
                batch.getContainerImage(),
                batch.getSubmittedBy(),
                submissionDto.getJobSettings()));

        return response;
    }

    @Transactional
    public MlOpsBatch createInitialBatch(MlOpsBatchSubmissionDto submissionDto) {
        MlOpsBatch batch = new MlOpsBatch();
        batch.setTestingDataNvrsVersion(submissionDto.getTestingDataNvrsVersion());
        batch.setPromptsVersion(submissionDto.getPromptsVersion());
        batch.setKnownNonIssuesVersion(submissionDto.getKnownNonIssuesVersion());
        batch.setContainerImage(submissionDto.getSastAiImage());
        batch.setSubmittedBy(submissionDto.getSubmittedBy() != null ? submissionDto.getSubmittedBy() : "unknown");
        batch.setStatus(BatchStatus.PROCESSING);

        mlOpsBatchRepository.persist(batch);
        LOGGER.info(
                "Created new MLOps batch with ID: {} for testing data version: {}",
                batch.getId(),
                submissionDto.getTestingDataNvrsVersion());

        return batch;
    }

    /**
     * Asynchronously processes an MLOps batch by fetching NVRs from DVC and creating individual jobs.
     */
    public void executeBatchProcessing(
            @Nonnull Long batchId,
            @Nonnull String testingDataNvrsVersion,
            @Nonnull String promptsVersion,
            @Nonnull String knownNonIssuesVersion,
            @Nonnull String containerImage,
            String submittedBy,
            JobSettingsDto jobSettingsDto) {

        LOGGER.info("Starting MLOps batch processing for batch ID: {}", batchId);

        try {
            // Fetch NVR list from DVC
            List<String> nvrs = dvcService.getNvrList(testingDataNvrsVersion);

            if (nvrs.isEmpty()) {
                LOGGER.warn("No NVRs found for batch {}, marking as COMPLETED_EMPTY", batchId);
                updateBatchStatus(batchId, BatchStatus.COMPLETED_EMPTY);
                return;
            }

            LOGGER.info("Batch {}: Fetched {} NVRs from DVC", batchId, nvrs.size());
            updateBatchTotalJobs(batchId, nvrs.size());

            // Process each NVR sequentially
            processNvrs(
                    batchId, nvrs, promptsVersion, knownNonIssuesVersion, containerImage, submittedBy, jobSettingsDto);

            // Update final batch status
            updateFinalBatchStatus(batchId);

        } catch (DvcException e) {
            LOGGER.error("DVC error while processing batch {}: {}", batchId, e.getMessage(), e);
            updateBatchStatus(batchId, BatchStatus.FAILED);
        } catch (Exception e) {
            LOGGER.error("Unexpected error while processing batch {}: {}", batchId, e.getMessage(), e);
            updateBatchStatus(batchId, BatchStatus.FAILED);
        }
    }

    /**
     * Processes a list of NVRs with controlled parallelism using a rolling window.
     * Uses a semaphore to ensure max N jobs run concurrently at any time.
     * As soon as one job completes, the next one starts immediately.
     */
    private void processNvrs(
            Long batchId,
            List<String> nvrs,
            String promptsVersion,
            String knownNonIssuesVersion,
            String containerImage,
            String submittedBy,
            JobSettingsDto jobSettingsDto) {

        LOGGER.info(
                "Batch {}: Starting parallel processing of {} NVRs (max {} parallel jobs with rolling window)",
                batchId,
                nvrs.size(),
                maxParallelJobs);

        // Semaphore to control max concurrent jobs
        final Semaphore semaphore = new Semaphore(maxParallelJobs);
        List<CompletableFuture<Void>> allFutures = new ArrayList<>();

        for (int i = 0; i < nvrs.size(); i++) {
            String nvr = nvrs.get(i);
            final int index = i + 1; // For logging

            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> {
                        try {
                            // Acquire permit - blocks if max parallel jobs already running
                            semaphore.acquire();
                            LOGGER.debug(
                                    "Batch {}: Acquired slot for NVR {}/{}: {} (available slots: {})",
                                    batchId,
                                    index,
                                    nvrs.size(),
                                    nvr,
                                    semaphore.availablePermits());

                            processSingleNvr(
                                    batchId,
                                    nvr,
                                    promptsVersion,
                                    knownNonIssuesVersion,
                                    containerImage,
                                    submittedBy,
                                    jobSettingsDto);

                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            LOGGER.error("Batch {}: Interrupted while waiting for slot for NVR: {}", batchId, nvr);
                        } finally {
                            // Release permit - allows next job to start
                            semaphore.release();
                            LOGGER.debug(
                                    "Batch {}: Released slot for NVR {} (available slots: {})",
                                    batchId,
                                    nvr,
                                    semaphore.availablePermits());
                        }
                    },
                    managedExecutor);

            allFutures.add(future);
        }

        // Wait for ALL jobs to complete
        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();

        LOGGER.info("Batch {}: All {} jobs completed", batchId, nvrs.size());
    }

    /**
     * Processes a single NVR - creates job and starts pipeline, then waits for completion.
     */
    private void processSingleNvr(
            Long batchId,
            String nvr,
            String promptsVersion,
            String knownNonIssuesVersion,
            String containerImage,
            String submittedBy,
            JobSettingsDto jobSettingsDto) {

        Long jobId = null;

        try {
            // Validate NVR
            if (!nvrResolutionService.isValidNvr(nvr)) {
                LOGGER.warn("Batch {}: Invalid NVR format: {}, skipping", batchId, nvr);
                incrementBatchFailedJobs(batchId);
                return;
            }

            // Create MlOpsJob with resolved NVR information
            final MlOpsJob createdJob = createMlOpsJobInNewTransaction(
                    batchId, nvr, promptsVersion, knownNonIssuesVersion, submittedBy, jobSettingsDto);
            jobId = createdJob.getId();

            // Generate pipeline parameters
            final List<Param> pipelineParams = parameterMapper.extractMlOpsPipelineParams(
                    createdJob, promptsVersion, knownNonIssuesVersion, containerImage);
            final String llmSecretName = "sast-ai-default-llm-creds";

            LOGGER.info("Batch {}: Starting pipeline for job {} (NVR: {})", batchId, jobId, nvr);

            // Start Tekton pipeline for MLOps job
            platformService.startSastAIWorkflowForMlOpsJob(batchId, jobId, pipelineParams, llmSecretName, this);

            // Wait for this job to complete
            boolean jobCompleted = waitForJobCompletion(jobId);

            if (jobCompleted) {
                LOGGER.info("Batch {}: Job {} completed successfully (NVR: {})", batchId, jobId, nvr);
            } else {
                LOGGER.warn("Batch {}: Job {} did not complete successfully (NVR: {})", batchId, jobId, nvr);
            }

        } catch (InvalidNvrException e) {
            LOGGER.error("Batch {}: NVR resolution error for '{}': {}", batchId, nvr, e.getMessage());
            incrementBatchFailedJobs(batchId);
            if (jobId != null) {
                updateMlOpsJobStatusInNewTransaction(jobId, JobStatus.FAILED);
            }
        } catch (Exception e) {
            LOGGER.error("Batch {}: Error processing NVR '{}': {}", batchId, nvr, e.getMessage(), e);
            incrementBatchFailedJobs(batchId);
            if (jobId != null) {
                updateMlOpsJobStatusInNewTransaction(jobId, JobStatus.FAILED);
            }
        }
    }

    /**
     * Creates an MlOpsJob entity with resolved NVR information in a new transaction.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public MlOpsJob createMlOpsJobInNewTransaction(
            Long batchId,
            String nvr,
            String promptsVersion,
            String knownNonIssuesVersion,
            String submittedBy,
            JobSettingsDto jobSettingsDto) {

        MlOpsBatch batch = mlOpsBatchRepository.findById(batchId);
        if (batch == null) {
            throw new IllegalStateException("MLOps batch not found: " + batchId);
        }

        MlOpsJob job = new MlOpsJob();
        job.setMlOpsBatch(batch);
        job.setPackageNvr(nvr);
        job.setSubmittedBy(submittedBy);
        job.setStatus(JobStatus.PENDING);

        // Resolve NVR components
        try {
            String projectName = nvrResolutionService.resolveProjectName(nvr);
            String projectVersion = nvrResolutionService.resolveProjectVersion(nvr);
            String packageName = nvrResolutionService.resolvePackageName(nvr);
            String sourceCodeUrl = nvrResolutionService.resolveSourceCodeUrl(nvr);
            String knownFalsePositivesUrl = nvrResolutionService.resolveKnownFalsePositivesUrl(nvr);

            job.setProjectName(projectName);
            job.setProjectVersion(projectVersion);
            job.setPackageName(packageName);
            job.setPackageSourceCodeUrl(sourceCodeUrl);
            job.setKnownFalsePositivesUrl(knownFalsePositivesUrl);

        } catch (InvalidNvrException e) {
            LOGGER.error("Failed to resolve NVR '{}': {}", nvr, e.getMessage());
            throw e;
        }

        // Map JobSettingsDto to MlOpsJobSettings entity if provided
        if (jobSettingsDto != null) {
            MlOpsJobSettings mlOpsJobSettings = JobSettingsMapper.INSTANCE.toEntity(jobSettingsDto);
            mlOpsJobSettings.setMlOpsJob(job);
            job.setMlOpsJobSettings(mlOpsJobSettings);
        }

        mlOpsJobRepository.persist(job);
        LOGGER.debug("Created MLOps job {} for NVR: {}", job.getId(), nvr);

        return job;
    }

    /**
     * Waits for a job to complete (either successfully or with failure).
     * Delegates to BatchOperationsHelper for common polling logic.
     */
    private boolean waitForJobCompletion(Long jobId) {
        return batchOperationsHelper.waitForJobCompletion(jobId, this::checkJobStatusInNewTransaction);
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public JobStatus checkJobStatusInNewTransaction(Long jobId) {
        MlOpsJob job = mlOpsJobRepository.findById(jobId);
        return job != null ? job.getStatus() : null;
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateBatchStatus(Long batchId, BatchStatus status) {
        MlOpsBatch batch = mlOpsBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setStatus(status);
            mlOpsBatchRepository.persist(batch);
            LOGGER.info("Updated batch {} status to {}", batchId, status);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateBatchTotalJobs(Long batchId, int totalJobs) {
        MlOpsBatch batch = mlOpsBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setTotalJobs(totalJobs);
            mlOpsBatchRepository.persist(batch);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void incrementBatchCompletedJobs(Long batchId) {
        MlOpsBatch batch = mlOpsBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setCompletedJobs(batch.getCompletedJobs() + 1);
            mlOpsBatchRepository.persist(batch);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void incrementBatchFailedJobs(Long batchId) {
        MlOpsBatch batch = mlOpsBatchRepository.findById(batchId);
        if (batch != null) {
            batch.setFailedJobs(batch.getFailedJobs() + 1);
            mlOpsBatchRepository.persist(batch);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateMlOpsJobStatusInNewTransaction(Long jobId, JobStatus status) {
        MlOpsJob job = mlOpsJobRepository.findById(jobId);
        if (job != null) {
            job.setStatus(status);
            mlOpsJobRepository.persist(job);
        }
    }

    /**
     * Updates final batch status based on job completion counts.
     * Uses BatchOperationsHelper to determine the appropriate final status.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateFinalBatchStatus(Long batchId) {
        MlOpsBatch batch = mlOpsBatchRepository.findById(batchId);
        if (batch == null) {
            LOGGER.error("Batch {} not found when updating final status", batchId);
            return;
        }

        int totalJobs = batch.getTotalJobs() != null ? batch.getTotalJobs() : 0;
        int completedJobs = batch.getCompletedJobs() != null ? batch.getCompletedJobs() : 0;
        int failedJobs = batch.getFailedJobs() != null ? batch.getFailedJobs() : 0;

        BatchStatus finalStatus = batchOperationsHelper.determineFinalBatchStatus(totalJobs, completedJobs, failedJobs);

        batch.setStatus(finalStatus);
        mlOpsBatchRepository.persist(batch);
        LOGGER.info(
                "Updated batch {} final status to {} (total={}, completed={}, failed={})",
                batchId,
                finalStatus,
                totalJobs,
                completedJobs,
                failedJobs);
    }

    /**
     * Retrieves a specific MLOps batch by ID.
     */
    public MlOpsBatchResponseDto getBatchById(Long batchId) {
        MlOpsBatch batch = mlOpsBatchRepository.findById(batchId);
        if (batch == null) {
            return null;
        }
        return convertToResponseDto(batch);
    }

    /**
     * Retrieves detailed batch information including all jobs and their metrics.
     */
    @Transactional
    public com.redhat.sast.api.v1.dto.response.MlOpsBatchDetailedResponseDto getBatchDetailedById(Long batchId) {
        MlOpsBatch batch = mlOpsBatchRepository.findById(batchId);
        if (batch == null) {
            return null;
        }

        // Build detailed response with jobs and metrics
        List<com.redhat.sast.api.v1.dto.response.MlOpsJobDetailedResponseDto> jobDtos =
                batch.getJobs().stream().map(this::convertJobToDetailedDto).toList();

        return com.redhat.sast.api.v1.dto.response.MlOpsBatchDetailedResponseDto.builder()
                .batchId(batch.getId())
                .testingDataNvrsVersion(batch.getTestingDataNvrsVersion())
                .promptsVersion(batch.getPromptsVersion())
                .knownNonIssuesVersion(batch.getKnownNonIssuesVersion())
                .sastAiImage(batch.getContainerImage())
                .submittedBy(batch.getSubmittedBy())
                .submittedAt(batch.getSubmittedAt())
                .status(batch.getStatus())
                .totalJobs(batch.getTotalJobs())
                .completedJobs(batch.getCompletedJobs())
                .failedJobs(batch.getFailedJobs())
                .lastUpdatedAt(batch.getLastUpdatedAt())
                .jobs(jobDtos)
                .build();
    }

    private com.redhat.sast.api.v1.dto.response.MlOpsJobDetailedResponseDto convertJobToDetailedDto(
            com.redhat.sast.api.model.MlOpsJob job) {
        // Build metrics DTO if metrics exist
        com.redhat.sast.api.v1.dto.response.MlOpsJobDetailedResponseDto.MlOpsJobMetricsDto metricsDto = null;

        if (job.getMlOpsJobMetrics() != null) {
            var metrics = job.getMlOpsJobMetrics();

            // Build confusion matrix DTO
            com.redhat.sast.api.v1.dto.response.MlOpsJobDetailedResponseDto.ConfusionMatrixDto cmDto = null;
            if (metrics.getCmTp() != null
                    || metrics.getCmFp() != null
                    || metrics.getCmTn() != null
                    || metrics.getCmFn() != null) {
                cmDto = com.redhat.sast.api.v1.dto.response.MlOpsJobDetailedResponseDto.ConfusionMatrixDto.builder()
                        .tp(metrics.getCmTp())
                        .fp(metrics.getCmFp())
                        .tn(metrics.getCmTn())
                        .fn(metrics.getCmFn())
                        .build();
            }

            metricsDto = com.redhat.sast.api.v1.dto.response.MlOpsJobDetailedResponseDto.MlOpsJobMetricsDto.builder()
                    .accuracy(metrics.getAccuracy())
                    .precision(metrics.getPrecision())
                    .recall(metrics.getRecall())
                    .f1Score(metrics.getF1Score())
                    .confusionMatrix(cmDto)
                    .build();
        }

        return com.redhat.sast.api.v1.dto.response.MlOpsJobDetailedResponseDto.builder()
                .jobId(job.getId())
                .packageNvr(job.getPackageNvr())
                .packageName(job.getPackageName())
                .projectName(job.getProjectName())
                .projectVersion(job.getProjectVersion())
                .packageSourceCodeUrl(job.getPackageSourceCodeUrl())
                .knownFalsePositivesUrl(job.getKnownFalsePositivesUrl())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .tektonUrl(job.getTektonUrl())
                .oshScanId(job.getOshScanId())
                .metrics(metricsDto)
                .build();
    }

    /**
     * Retrieves all MLOps batches with pagination.
     */
    public List<MlOpsBatchResponseDto> getAllBatches(int page, int size) {
        return mlOpsBatchRepository.findAll().page(page, size).stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    /**
     * Converts MlOpsBatch entity to response DTO.
     */
    private MlOpsBatchResponseDto convertToResponseDto(MlOpsBatch batch) {
        return MlOpsBatchResponseDto.builder()
                .batchId(batch.getId())
                .testingDataNvrsVersion(batch.getTestingDataNvrsVersion())
                .promptsVersion(batch.getPromptsVersion())
                .knownNonIssuesVersion(batch.getKnownNonIssuesVersion())
                .sastAiImage(batch.getContainerImage())
                .submittedBy(batch.getSubmittedBy())
                .submittedAt(batch.getSubmittedAt())
                .status(batch.getStatus())
                .totalJobs(batch.getTotalJobs())
                .completedJobs(batch.getCompletedJobs())
                .failedJobs(batch.getFailedJobs())
                .lastUpdatedAt(batch.getLastUpdatedAt())
                .build();
    }
}
