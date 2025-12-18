package com.redhat.sast.api.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.enums.PipelineRunStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobBatch;
import com.redhat.sast.api.model.MlOpsBatch;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.platform.KubernetesResourceManager;
import com.redhat.sast.api.platform.MlOpsPipelineRunWatcher;
import com.redhat.sast.api.platform.PipelineRunWatcher;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.repository.mlops.MlOpsBatchRepository;
import com.redhat.sast.api.repository.mlops.MlOpsJobRepository;
import com.redhat.sast.api.service.mlops.DataArtifactService;
import com.redhat.sast.api.service.mlops.DvcMetadataService;
import com.redhat.sast.api.service.mlops.MlOpsBatchService;
import com.redhat.sast.api.service.mlops.MlOpsExcelReportService;
import com.redhat.sast.api.service.mlops.MlOpsJobService;
import com.redhat.sast.api.service.mlops.MlOpsJobSettingsService;
import com.redhat.sast.api.service.mlops.MlOpsMetricsService;
import com.redhat.sast.api.service.mlops.MlOpsNodeFilterEvalService;
import com.redhat.sast.api.service.mlops.MlOpsNodeJudgeEvalService;
import com.redhat.sast.api.service.mlops.MlOpsNodeSummaryEvalService;
import com.redhat.sast.api.service.mlops.MlOpsTokenMetricsService;

import io.fabric8.knative.pkg.apis.Condition;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.PipelineRun;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for recovering orphaned jobs and batches (for example after pod crashes).
 *
 * This service performs the following actions:
 * - Runs on startup to immediately recover orphaned jobs/batches after pod restart
 * - Runs periodically (default: every 15 minutes) to catch any edge cases
 * - Reconciles database state with actual Kubernetes PipelineRun state
 * - Re-establishes watchers for still-running PipelineRuns
 * - Recalculates batch status based on child job statuses
 * - Handles both regular Jobs/JobBatches and MLOps Jobs/Batches
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class JobRecoveryService {

    private final JobRepository jobRepository;
    private final MlOpsJobRepository mlOpsJobRepository;
    private final JobBatchRepository jobBatchRepository;
    private final MlOpsBatchRepository mlOpsBatchRepository;

    private final TektonClient tektonClient;
    private final KubernetesResourceManager resourceManager;
    private final ManagedExecutor managedExecutor;

    private final JobService jobService;
    private final MlOpsJobService mlOpsJobService;
    private final JobBatchService jobBatchService;
    private final MlOpsBatchService mlOpsBatchService;
    private final BatchOperationsHelper batchOperationsHelper;

    private final DvcMetadataService dvcMetadataService;
    private final DataArtifactService dataArtifactService;

    private final MlOpsJobSettingsService mlOpsJobSettingsService;
    private final MlOpsMetricsService mlOpsMetricsService;
    private final MlOpsTokenMetricsService mlOpsTokenMetricsService;
    private final MlOpsExcelReportService mlOpsExcelReportService;
    private final MlOpsNodeFilterEvalService mlOpsNodeFilterEvalService;
    private final MlOpsNodeJudgeEvalService mlOpsNodeJudgeEvalService;
    private final MlOpsNodeSummaryEvalService mlOpsNodeSummaryEvalService;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    @ConfigProperty(name = "recovery.orphaned.job.threshold.minutes", defaultValue = "5")
    int orphanedJobThresholdMinutes;

    @ConfigProperty(name = "recovery.max.jobs.per.cycle", defaultValue = "100")
    int maxJobsPerCycle;

    /**
     * Performs full recovery of orphaned jobs and batches.
     */
    @Transactional
    public void performFullRecovery() {
        try {
            List<Job> orphanedJobs = findOrphanedJobs();
            if (!orphanedJobs.isEmpty()) {
                recoverOrphanedJobsSafely(orphanedJobs);
            }

            List<MlOpsJob> orphanedMlOpsJobs = findOrphanedMlOpsJobs();
            if (!orphanedMlOpsJobs.isEmpty()) {
                recoverOrphanedMlOpsJobsSafely(orphanedMlOpsJobs);
            }

            List<JobBatch> stuckBatches = findStuckJobBatches();
            if (!stuckBatches.isEmpty()) {
                recoverStuckJobBatchesSafely(stuckBatches);
            }

            List<MlOpsBatch> stuckMlOpsBatches = findStuckMlOpsBatches();
            if (!stuckMlOpsBatches.isEmpty()) {
                recoverStuckMlOpsBatchesSafely(stuckMlOpsBatches);
            }

            LOGGER.debug("Full recovery cycle completed");
        } catch (Exception e) {
            LOGGER.error("Error during full recovery", e);
        }
    }

    /**
     * Recovers a list of orphaned jobs with isolated error handling.
     *
     * @param orphanedJobs list of jobs to recover
     */
    private void recoverOrphanedJobsSafely(List<Job> orphanedJobs) {
        LOGGER.debug("Found {} potentially orphaned regular jobs", orphanedJobs.size());
        for (Job job : orphanedJobs) {
            try {
                recoverJob(job);
            } catch (Exception e) {
                LOGGER.error("Failed to recover job {}, continuing with remaining jobs", job.getId(), e);
            }
        }
    }

    /**
     * Recovers a list of orphaned MLOps jobs with isolated error handling.
     *
     * @param orphanedJobs list of MLOps jobs to recover
     */
    private void recoverOrphanedMlOpsJobsSafely(List<MlOpsJob> orphanedJobs) {
        LOGGER.debug("Found {} potentially orphaned MLOps jobs", orphanedJobs.size());
        for (MlOpsJob job : orphanedJobs) {
            try {
                recoverMlOpsJob(job);
            } catch (Exception e) {
                LOGGER.error("Failed to recover MLOps job {}, continuing with remaining jobs", job.getId(), e);
            }
        }
    }

    /**
     * Recovers a list of stuck job batches with isolated error handling.
     *
     * @param stuckBatches list of batches to recover
     */
    private void recoverStuckJobBatchesSafely(List<JobBatch> stuckBatches) {
        LOGGER.info("Found {} potentially stuck job batches", stuckBatches.size());
        for (JobBatch batch : stuckBatches) {
            try {
                recoverJobBatch(batch);
            } catch (Exception e) {
                LOGGER.error("Failed to recover job batch {}, continuing with remaining batches", batch.getId(), e);
            }
        }
    }

    /**
     * Recovers a list of stuck MLOps batches with isolated error handling.
     *
     * @param stuckBatches list of MLOps batches to recover
     */
    private void recoverStuckMlOpsBatchesSafely(List<MlOpsBatch> stuckBatches) {
        LOGGER.info("Found {} potentially stuck MLOps batches", stuckBatches.size());
        for (MlOpsBatch batch : stuckBatches) {
            try {
                recoverMlOpsBatch(batch);
            } catch (Exception e) {
                LOGGER.error("Failed to recover MLOps batch {}, continuing with remaining batches", batch.getId(), e);
            }
        }
    }

    /**
     * Finds potentially orphaned regular jobs.
     */
    private List<Job> findOrphanedJobs() {
        Instant threshold = Instant.now().minus(orphanedJobThresholdMinutes, ChronoUnit.MINUTES);
        return jobRepository.findOrphanedJobs(threshold, maxJobsPerCycle);
    }

    /**
     * Finds potentially orphaned MLOps jobs.
     */
    private List<MlOpsJob> findOrphanedMlOpsJobs() {
        LocalDateTime threshold = LocalDateTime.now().minus(orphanedJobThresholdMinutes, ChronoUnit.MINUTES);
        return mlOpsJobRepository.findOrphanedJobs(threshold, maxJobsPerCycle);
    }

    /**
     * Finds potentially stuck job batches.
     */
    private List<JobBatch> findStuckJobBatches() {
        // Use 2x threshold for batches as they take longer to process
        Instant threshold = Instant.now().minus((long) orphanedJobThresholdMinutes * 2, ChronoUnit.MINUTES);
        return jobBatchRepository.findStuckBatches(threshold);
    }

    /**
     * Finds potentially stuck MLOps batches.
     */
    private List<MlOpsBatch> findStuckMlOpsBatches() {
        // Use 2x threshold for batches as they take longer to process
        LocalDateTime threshold = LocalDateTime.now().minus((long) orphanedJobThresholdMinutes * 2, ChronoUnit.MINUTES);
        return mlOpsBatchRepository.findStuckBatches(threshold);
    }

    /**
     * Recovers a single regular job by reconciling with Kubernetes state.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void recoverJob(Job job) {
        Long jobId = job.getId();
        String tektonUrl = job.getTektonUrl();

        if (tektonUrl == null || tektonUrl.isBlank()) {
            LOGGER.warn("Job {} has no tektonUrl, marking as FAILED", jobId);
            jobService.updateJobStatus(jobId, JobStatus.FAILED);
            return;
        }

        String pipelineRunName = resourceManager.extractPipelineRunName(tektonUrl);
        if (pipelineRunName == null) {
            LOGGER.error("Cannot extract PipelineRun name from URL: {}", tektonUrl);
            jobService.updateJobStatus(jobId, JobStatus.FAILED);
            return;
        }

        PipelineRunStatus prStatus = getPipelineRunStatus(pipelineRunName);

        switch (prStatus) {
            case NOT_FOUND -> {
                LOGGER.warn("PipelineRun {} not found for job {}, marking FAILED", pipelineRunName, jobId);
                jobService.updateJobStatus(jobId, JobStatus.FAILED);
            }
            case SUCCEEDED -> {
                LOGGER.info(
                        "PipelineRun {} succeeded but watcher lost for job {}, marking COMPLETED",
                        pipelineRunName,
                        jobId);
                jobService.updateJobStatus(jobId, JobStatus.COMPLETED);
            }
            case FAILED -> {
                LOGGER.warn("PipelineRun {} failed for job {}, marking FAILED", pipelineRunName, jobId);
                jobService.updateJobStatus(jobId, JobStatus.FAILED);
            }
            case CANCELLED -> {
                LOGGER.info("PipelineRun {} was cancelled for job {}", pipelineRunName, jobId);
                if (job.getStatus() != JobStatus.CANCELLED) {
                    jobService.updateJobStatus(jobId, JobStatus.CANCELLED);
                }
            }
            case RUNNING -> {
                LOGGER.info("Re-establishing watcher for running PipelineRun {} for job {}", pipelineRunName, jobId);
                reestablishWatcher(jobId, pipelineRunName, false);
            }
            case ERROR -> LOGGER.error("Error checking PipelineRun status for job {}, will retry next cycle", jobId);
            default -> LOGGER.error("Unexpected PipelineRunStatus {} for job {}, cannot recover", prStatus, jobId);
        }
    }

    /**
     * Recovers a single MLOps job by reconciling with Kubernetes state.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void recoverMlOpsJob(MlOpsJob job) {
        Long jobId = job.getId();
        Long batchId = job.getMlOpsBatch().getId();
        String tektonUrl = job.getTektonUrl();

        if (tektonUrl == null || tektonUrl.isBlank()) {
            LOGGER.warn("MLOps job {} has no tektonUrl, marking as FAILED", jobId);
            mlOpsJobService.updateJobStatus(jobId, JobStatus.FAILED);
            mlOpsBatchService.incrementBatchFailedJobs(batchId);
            return;
        }

        String pipelineRunName = resourceManager.extractPipelineRunName(tektonUrl);
        if (pipelineRunName == null) {
            LOGGER.error("Cannot extract PipelineRun name from MLOps job {}", jobId);
            mlOpsJobService.updateJobStatus(jobId, JobStatus.FAILED);
            mlOpsBatchService.incrementBatchFailedJobs(batchId);
            return;
        }

        PipelineRunStatus prStatus = getPipelineRunStatus(pipelineRunName);

        switch (prStatus) {
            case NOT_FOUND -> {
                LOGGER.warn("PipelineRun {} not found for MLOps job {}", pipelineRunName, jobId);
                mlOpsJobService.updateJobStatus(jobId, JobStatus.FAILED);
                mlOpsBatchService.incrementBatchFailedJobs(batchId);
            }
            case SUCCEEDED -> {
                LOGGER.info("PipelineRun {} succeeded for MLOps job {}, marking COMPLETED", pipelineRunName, jobId);
                mlOpsJobService.updateJobStatus(jobId, JobStatus.COMPLETED);
                mlOpsBatchService.incrementBatchCompletedJobs(batchId);
            }
            case FAILED -> {
                LOGGER.warn("PipelineRun {} failed for MLOps job {}", pipelineRunName, jobId);
                mlOpsJobService.updateJobStatus(jobId, JobStatus.FAILED);
                mlOpsBatchService.incrementBatchFailedJobs(batchId);
            }
            case CANCELLED -> {
                if (job.getStatus() != JobStatus.CANCELLED) {
                    mlOpsJobService.updateJobStatus(jobId, JobStatus.CANCELLED);
                }
            }
            case RUNNING -> {
                LOGGER.info("Re-establishing watcher for MLOps job {}", jobId);
                reestablishWatcher(jobId, pipelineRunName, true);
            }
            case ERROR -> LOGGER.error("Error checking PipelineRun for MLOps job {}", jobId);
            default ->
                LOGGER.error("Unexpected PipelineRunStatus {} for MLOps job {}, cannot recover", prStatus, jobId);
        }
    }

    /**
     * Recovers a job batch by recalculating counters from child job statuses.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void recoverJobBatch(JobBatch batch) {
        Long batchId = batch.getId();
        LOGGER.info("Recovering job batch {}", batchId);

        List<Job> jobs = batch.getJobs();
        if (jobs.isEmpty()) {
            LOGGER.warn("Batch {} has no jobs, marking COMPLETED_EMPTY", batchId);
            jobBatchService.updateBatchStatus(batchId, BatchStatus.COMPLETED_EMPTY);
            return;
        }

        int total = jobs.size();
        int completed = (int)
                jobs.stream().filter(j -> j.getStatus() == JobStatus.COMPLETED).count();
        int failed = (int)
                jobs.stream().filter(j -> j.getStatus() == JobStatus.FAILED).count();
        int cancelled = (int)
                jobs.stream().filter(j -> j.getStatus() == JobStatus.CANCELLED).count();
        int running = (int) jobs.stream()
                .filter(j -> j.getStatus() == JobStatus.RUNNING
                        || j.getStatus() == JobStatus.SCHEDULED
                        || j.getStatus() == JobStatus.PENDING)
                .count();

        BatchStatus newStatus;
        if (running > 0) {
            newStatus = BatchStatus.PROCESSING;
            LOGGER.debug("Batch {} still has {} running jobs, keeping PROCESSING", batchId, running);
        } else {
            newStatus = batchOperationsHelper.determineFinalBatchStatus(total, completed, failed);
            LOGGER.debug(
                    "Batch {} recovery: total={}, completed={}, failed={}, cancelled={}, newStatus={}",
                    batchId,
                    total,
                    completed,
                    failed,
                    cancelled,
                    newStatus);
        }

        jobBatchService.updateBatchStatus(batchId, newStatus, total, completed, failed);
    }

    /**
     * Recovers an MLOps batch by recalculating counters from child job statuses.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void recoverMlOpsBatch(MlOpsBatch batch) {
        Long batchId = batch.getId();
        LOGGER.info("Recovering MLOps batch {}", batchId);

        List<MlOpsJob> jobs = batch.getJobs();
        if (jobs.isEmpty()) {
            LOGGER.warn("MLOps batch {} has no jobs, marking COMPLETED_EMPTY", batchId);
            batch.setStatus(BatchStatus.COMPLETED_EMPTY);
            mlOpsBatchRepository.persist(batch);
            return;
        }

        int total = jobs.size();
        int completed = (int)
                jobs.stream().filter(j -> j.getStatus() == JobStatus.COMPLETED).count();
        int failed = (int)
                jobs.stream().filter(j -> j.getStatus() == JobStatus.FAILED).count();
        int running = (int) jobs.stream()
                .filter(j -> j.getStatus() == JobStatus.RUNNING
                        || j.getStatus() == JobStatus.SCHEDULED
                        || j.getStatus() == JobStatus.PENDING)
                .count();

        BatchStatus newStatus;
        if (running > 0) {
            newStatus = BatchStatus.PROCESSING;
            LOGGER.info("MLOps batch {} still has {} running jobs", batchId, running);
        } else {
            newStatus = batchOperationsHelper.determineFinalBatchStatus(total, completed, failed);
            LOGGER.info(
                    "MLOps batch {} recovery: total={}, completed={}, failed={}, newStatus={}",
                    batchId,
                    total,
                    completed,
                    failed,
                    newStatus);
        }

        batch.setTotalJobs(total);
        batch.setCompletedJobs(completed);
        batch.setFailedJobs(failed);
        batch.setStatus(newStatus);
        mlOpsBatchRepository.persist(batch);
    }

    /**
     * Gets the status of a PipelineRun from Kubernetes.
     */
    private PipelineRunStatus getPipelineRunStatus(String pipelineRunName) {
        try {
            PipelineRun pr = resourceManager.getPipelineRun(pipelineRunName);

            if (pr == null) {
                return PipelineRunStatus.NOT_FOUND;
            }

            if (resourceManager.isPipelineCompleted(pr)) {
                // Check if succeeded or failed
                Optional<Condition> succeededCondition = pr.getStatus().getConditions().stream()
                        .filter(c -> "Succeeded".equalsIgnoreCase(c.getType()))
                        .findFirst();

                if (succeededCondition.isPresent()) {
                    String status = succeededCondition.get().getStatus();
                    if ("True".equalsIgnoreCase(status)) {
                        return PipelineRunStatus.SUCCEEDED;
                    } else if ("False".equalsIgnoreCase(status)) {
                        String reason = succeededCondition.get().getReason();
                        if ("Cancelled".equalsIgnoreCase(reason)) {
                            return PipelineRunStatus.CANCELLED;
                        }
                        return PipelineRunStatus.FAILED;
                    }
                }
            }

            return PipelineRunStatus.RUNNING;

        } catch (KubernetesClientException e) {
            LOGGER.error("Kubernetes error checking PipelineRun {}", pipelineRunName, e);
            return PipelineRunStatus.ERROR;
        } catch (Exception e) {
            LOGGER.error("Error getting PipelineRun status for {}", pipelineRunName, e);
            return PipelineRunStatus.ERROR;
        }
    }

    /**
     * Re-establishes a watcher for a still-running PipelineRun.
     */
    private void reestablishWatcher(Long jobId, String pipelineRunName, boolean isMlOps) {
        managedExecutor.execute(() -> {
            try {
                if (isMlOps) {
                    watchMlOpsPipelineRunForRecovery(jobId, pipelineRunName);
                } else {
                    watchPipelineRunForRecovery(jobId, pipelineRunName);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to reestablish watcher for job {}", jobId, e);
            }
        });
    }

    /**
     * Watches a PipelineRun for recovery of a regular job.
     */
    private void watchPipelineRunForRecovery(Long jobId, String pipelineRunName) {
        LOGGER.info("Starting recovery watcher for PipelineRun: {}", pipelineRunName);
        final CompletableFuture<Void> future = new CompletableFuture<>();

        try (var ignored = tektonClient
                .v1()
                .pipelineRuns()
                .inNamespace(namespace)
                .withName(pipelineRunName)
                .watch(new PipelineRunWatcher(
                        pipelineRunName, jobId, future, jobService, dvcMetadataService, dataArtifactService))) {
            future.join();
            LOGGER.info("Recovery watcher for PipelineRun {} completed", pipelineRunName);
        } catch (Exception e) {
            LOGGER.error("Recovery watcher failed for {}", pipelineRunName, e);
        }
    }

    /**
     * Watches a PipelineRun for recovery of an MLOps job.
     */
    private void watchMlOpsPipelineRunForRecovery(Long jobId, String pipelineRunName) {
        MlOpsJob job = mlOpsJobRepository.findById(jobId);
        if (job == null) {
            LOGGER.error("MLOps job {} not found for watcher recovery", jobId);
            return;
        }

        Long batchId = job.getMlOpsBatch().getId();

        LOGGER.info("Starting recovery watcher for MLOps PipelineRun: {}", pipelineRunName);
        final CompletableFuture<Void> future = new CompletableFuture<>();

        try (var ignored = tektonClient
                .v1()
                .pipelineRuns()
                .inNamespace(namespace)
                .withName(pipelineRunName)
                .watch(new MlOpsPipelineRunWatcher(
                        pipelineRunName,
                        jobId,
                        batchId,
                        future,
                        mlOpsJobService,
                        mlOpsJobSettingsService,
                        mlOpsMetricsService,
                        mlOpsTokenMetricsService,
                        mlOpsExcelReportService,
                        mlOpsNodeFilterEvalService,
                        mlOpsNodeJudgeEvalService,
                        mlOpsNodeSummaryEvalService,
                        mlOpsBatchService))) {
            future.join();
            LOGGER.info("Recovery watcher for MLOps PipelineRun {} completed", pipelineRunName);
        } catch (Exception e) {
            LOGGER.error("Recovery watcher for MLOps {} failed", pipelineRunName, e);
        }
    }
}
