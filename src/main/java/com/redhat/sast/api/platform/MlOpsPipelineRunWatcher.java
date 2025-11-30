package com.redhat.sast.api.platform;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.service.MlOpsExcelReportService;
import com.redhat.sast.api.service.MlOpsJobService;
import com.redhat.sast.api.service.MlOpsJobSettingsService;
import com.redhat.sast.api.service.MlOpsMetricsService;
import com.redhat.sast.api.service.MlOpsNodeJudgeEvalService;
import com.redhat.sast.api.service.MlOpsNodeSummaryEvalService;
import com.redhat.sast.api.service.MlOpsTokenMetricsService;

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
    private final MlOpsJobSettingsService mlOpsJobSettingsService;
    private final MlOpsMetricsService mlOpsMetricsService;
    private final MlOpsTokenMetricsService mlOpsTokenMetricsService;
    private final MlOpsExcelReportService mlOpsExcelReportService;
    private final MlOpsNodeJudgeEvalService mlOpsNodeJudgeEvalService;
    private final MlOpsNodeSummaryEvalService mlOpsNodeSummaryEvalService;
    private final Object mlOpsBatchService; // Object to avoid circular dependency

    // Flag to prevent duplicate processing of completion event
    private volatile boolean completionProcessed = false;

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
        // Prevent duplicate processing - Kubernetes may send multiple MODIFIED events
        if (completionProcessed) {
            LOGGER.debug(
                    "MLOps PipelineRun {} completion already processed for job {}, skipping", pipelineRunName, jobId);
            return;
        }
        completionProcessed = true;

        LOGGER.info("MLOps PipelineRun {} succeeded for job {}", pipelineRunName, jobId);

        // Extract and save job settings from PipelineRun parameters
        try {
            mlOpsJobSettingsService.extractAndSaveJobSettings(jobId, pipelineRun);
            LOGGER.debug("Job settings extraction completed for MLOps job {}", jobId);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to extract job settings for MLOps job {}, but job will still be marked as completed",
                    jobId,
                    e);
        }

        // Extract and save workflow metrics
        try {
            mlOpsMetricsService.extractAndSaveMetrics(jobId, pipelineRun);
            LOGGER.debug("Metrics extraction completed for MLOps job {}", jobId);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to extract metrics for MLOps job {}, but job will still be marked as completed", jobId, e);
        }

        // Get PipelineRun UID for S3 path
        String pipelineRunUid = pipelineRun.getMetadata().getUid();

        // Fetch and save token usage metrics from S3
        try {
            mlOpsTokenMetricsService.fetchAndSaveTokenMetrics(jobId, pipelineRunUid);
            LOGGER.debug("Token metrics extraction completed for MLOps job {}", jobId);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to fetch token metrics for MLOps job {}, but job will still be marked as completed",
                    jobId,
                    e);
        }

        // Fetch and save Excel report from S3
        try {
            mlOpsExcelReportService.fetchAndSaveExcelReport(jobId, pipelineRunUid);
            LOGGER.debug("Excel report parsing completed for MLOps job {}", jobId);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to fetch Excel report for MLOps job {}, but job will still be marked as completed",
                    jobId,
                    e);
        }

        // Extract and save judge node evaluation (optional - may not exist)
        try {
            mlOpsNodeJudgeEvalService.extractAndSaveJudgeEval(jobId, pipelineRun);
            LOGGER.debug("Judge node evaluation extraction completed for MLOps job {}", jobId);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to extract judge node evaluation for MLOps job {}, but job will still be marked as completed",
                    jobId,
                    e);
        }

        // Extract and save summary node evaluation (optional - may not exist)
        try {
            mlOpsNodeSummaryEvalService.extractAndSaveSummaryEval(jobId, pipelineRun);
            LOGGER.debug("Summary node evaluation extraction completed for MLOps job {}", jobId);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to extract summary node evaluation for MLOps job {}, but job will still be marked as completed",
                    jobId,
                    e);
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
