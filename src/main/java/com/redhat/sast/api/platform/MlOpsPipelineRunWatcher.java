package com.redhat.sast.api.platform;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.service.MlOpsJobService;
import com.redhat.sast.api.service.MlOpsMetricsService;

import io.fabric8.knative.pkg.apis.Condition;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.tekton.v1.PipelineRun;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Watches Tekton PipelineRun execution and updates MLOps job status accordingly.
 * Also extracts and stores workflow metrics when pipeline completes successfully.
 */
@RequiredArgsConstructor
@Slf4j
public class MlOpsPipelineRunWatcher implements Watcher<PipelineRun> {

    private final String pipelineRunName;
    private final Long jobId;
    private final Long batchId;
    private final CompletableFuture<Void> future;
    private final MlOpsJobService mlOpsJobService;
    private final MlOpsMetricsService mlOpsMetricsService;
    private final Object mlOpsBatchService; // Object to avoid circular dependency

    @Override
    public void eventReceived(Action action, PipelineRun pipelineRun) {
        try {
            LOGGER.debug("Received {} event for MLOps PipelineRun: {}", action, pipelineRunName);

            if (action == Action.MODIFIED || action == Action.ADDED) {
                handlePipelineRunEvent(pipelineRun);
            } else if (action == Action.DELETED) {
                LOGGER.warn("MLOps PipelineRun {} was deleted unexpectedly.", pipelineRunName);
                mlOpsJobService.updateJobStatus(jobId, JobStatus.FAILED);
                future.complete(null);
            }
        } catch (Exception e) {
            LOGGER.error("Error processing MLOps PipelineRun event for job {}", jobId, e);
            mlOpsJobService.updateJobStatus(jobId, JobStatus.FAILED);
            future.complete(null);
        }
    }

    @Override
    public void onClose(WatcherException cause) {
        if (cause != null) {
            LOGGER.error("MLOps PipelineRun watcher closed with error for job {}: {}", jobId, cause.getMessage());
            mlOpsJobService.updateJobStatus(jobId, JobStatus.FAILED);
        } else {
            LOGGER.debug("MLOps PipelineRun watcher closed normally for job {}", jobId);
        }
        future.complete(null);
    }

    private void handlePipelineRunEvent(PipelineRun pipelineRun) {
        if (pipelineRun.getStatus() == null || pipelineRun.getStatus().getConditions() == null) {
            LOGGER.debug("MLOps PipelineRun {} has no status yet", pipelineRunName);
            return;
        }

        Optional<Condition> condition = pipelineRun.getStatus().getConditions().stream()
                .filter(c -> "Succeeded".equalsIgnoreCase(c.getType()))
                .findFirst();

        if (condition.isEmpty()) {
            LOGGER.debug("MLOps PipelineRun {} has no 'Succeeded' condition yet", pipelineRunName);
            return;
        }

        Condition succeededCondition = condition.get();
        String status = succeededCondition.getStatus();

        if ("True".equalsIgnoreCase(status)) {
            handleSuccessfulPipeline(pipelineRun);
        } else if ("False".equalsIgnoreCase(status)) {
            handleFailedPipeline(succeededCondition);
        } else if ("Unknown".equalsIgnoreCase(status)) {
            handleRunningPipeline(succeededCondition);
        }
    }

    /**
     * Handles successful pipeline completion
     */
    private void handleSuccessfulPipeline(PipelineRun pipelineRun) {
        LOGGER.info("MLOps PipelineRun {} succeeded for job {}", pipelineRunName, jobId);

        // Extract and save workflow metrics
        try {
            mlOpsMetricsService.extractAndSaveMetrics(jobId, pipelineRun);
            LOGGER.debug("Metrics extraction completed for MLOps job {}", jobId);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to extract metrics for MLOps job {}, but job will still be marked as completed", jobId, e);
        }

        // Update job status - wrapped in try-catch to prevent watcher from crashing
        try {
            mlOpsJobService.updateJobStatus(jobId, JobStatus.COMPLETED);
            // Increment batch completed jobs counter
            if (mlOpsBatchService instanceof com.redhat.sast.api.service.MlOpsBatchService batchSvc) {
                batchSvc.incrementBatchCompletedJobs(batchId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update MLOps job {} status to COMPLETED: {}", jobId, e.getMessage(), e);
        }

        future.complete(null);
    }

    /**
     * Handles failed pipeline execution
     */
    private void handleFailedPipeline(Condition condition) {
        LOGGER.error(
                "MLOps PipelineRun {} failed for job {}. Reason: {}, Message: {}",
                pipelineRunName,
                jobId,
                condition.getReason(),
                condition.getMessage());

        try {
            if (condition.getReason() == null || !condition.getReason().equalsIgnoreCase("Cancelled")) {
                mlOpsJobService.updateJobStatus(jobId, JobStatus.FAILED);
                // Increment batch failed jobs counter
                if (mlOpsBatchService instanceof com.redhat.sast.api.service.MlOpsBatchService batchSvc) {
                    batchSvc.incrementBatchFailedJobs(batchId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update MLOps job {} status to FAILED: {}", jobId, e.getMessage(), e);
        }

        future.complete(null);
    }

    /**
     * Handles pipeline in running state
     */
    private void handleRunningPipeline(Condition condition) {
        if ("Unknown".equalsIgnoreCase(condition.getStatus())
                && condition.getReason() != null
                && condition.getReason().contains("Running")) {
            LOGGER.info("MLOps PipelineRun {} is running for job {}", pipelineRunName, jobId);
            try {
                mlOpsJobService.updateJobStatus(jobId, JobStatus.RUNNING);
            } catch (Exception e) {
                LOGGER.error("Failed to update MLOps job {} status to RUNNING: {}", jobId, e.getMessage(), e);
            }
        }
    }
}
