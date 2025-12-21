package com.redhat.sast.api.platform;

import java.util.concurrent.CompletableFuture;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.service.JobService;
import com.redhat.sast.api.service.LeaderElectionService;
import com.redhat.sast.api.service.mlops.DataArtifactService;
import com.redhat.sast.api.service.mlops.DvcMetadataService;

import io.fabric8.knative.pkg.apis.Condition;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.tekton.v1.PipelineRun;
import lombok.extern.slf4j.Slf4j;

/**
 * Watcher implementation for monitoring Tekton PipelineRun status changes.
 * Updates job status in the database based on pipeline execution results.
 * Extends AbstractLeaderAwareWatcher to avoid duplicate processing when leadership changes.
 */
@Slf4j
public class PipelineRunWatcher extends AbstractLeaderAwareWatcher implements Watcher<PipelineRun> {

    private static final String SUCCEEDED_CONDITION = "Succeeded";
    private static final String STATUS_TRUE = "True";
    private static final String STATUS_FALSE = "False";

    private final String pipelineRunName;
    private final JobService jobService;
    private final DvcMetadataService dvcMetadataService;
    private final DataArtifactService dataArtifactService;

    public PipelineRunWatcher(
            String pipelineRunName,
            long jobId,
            CompletableFuture<Void> future,
            JobService jobService,
            DvcMetadataService dvcMetadataService,
            DataArtifactService dataArtifactService,
            LeaderElectionService leaderElectionService) {
        super(leaderElectionService, future, jobId);
        this.pipelineRunName = pipelineRunName;
        this.jobService = jobService;
        this.dvcMetadataService = dvcMetadataService;
        this.dataArtifactService = dataArtifactService;
    }

    @Override
    public void eventReceived(Action action, PipelineRun pipelineRun) {
        LOGGER.info(
                "Watcher event: {} for PipelineRun: {}",
                action,
                pipelineRun.getMetadata().getName());

        var pipelineSuccessCondition = findSuccessCondition(pipelineRun);
        if (pipelineSuccessCondition.isEmpty()) {
            return;
        }

        var condition = pipelineSuccessCondition.get();
        switch (condition.getStatus()) {
            case STATUS_TRUE -> handleSuccessfulPipeline(pipelineRun);
            case STATUS_FALSE -> handleFailedPipeline(condition);
            default -> handleRunningPipeline(condition);
        }
    }

    @Override
    public void onClose(WatcherException cause) {
        if (cause != null) {
            LOGGER.error("Watcher for {} closed due to an error.", pipelineRunName, cause);
            future.completeExceptionally(cause);
        } else {
            LOGGER.warn("Watcher for {} closed cleanly without a final status.", pipelineRunName);
            future.complete(null);
        }
    }

    /**
     * Finds the 'Succeeded' condition from pipeline status
     */
    private java.util.Optional<Condition> findSuccessCondition(PipelineRun pipelineRun) {
        var status = pipelineRun.getStatus();
        if (status == null || status.getConditions() == null) {
            return java.util.Optional.empty();
        }

        return status.getConditions().stream()
                .filter(condition -> SUCCEEDED_CONDITION.equals(condition.getType()))
                .findFirst();
    }

    /**
     * Handles successful pipeline completion
     */
    private void handleSuccessfulPipeline(PipelineRun pipelineRun) {
        if (shouldSkipDueToLeadership("completion")) return;

        LOGGER.info("PipelineRun {} succeeded.", pipelineRunName);

        executeWithErrorHandling(
                () -> dvcMetadataService.updateDvcMetadata(jobId, pipelineRun),
                "DVC metadata successfully extracted for job {}",
                "Failed to extract DVC metadata for job {}, but job will still be marked as completed");

        executeWithErrorHandling(
                () -> {
                    var job = jobService.getJobEntityById(jobId);
                    if (job != null) {
                        dataArtifactService.createJobExecutionArtifacts(job, pipelineRun);
                    }
                },
                "Data artifacts successfully created for job {}",
                "Failed to create data artifacts for job {}, but job will still be marked as completed");

        if (shouldSkipDueToLeadership("status update")) return;

        jobService.updateJobStatus(jobId, JobStatus.COMPLETED);
        future.complete(null);
    }

    /**
     * Handles failed pipeline execution
     */
    private void handleFailedPipeline(Condition condition) {
        if (shouldSkipDueToLeadership("failure processing")) return;

        LOGGER.error(
                "PipelineRun {} failed. Reason: {}, Message: {}",
                pipelineRunName,
                condition.getReason(),
                condition.getMessage());

        if (condition.getReason() == null || !condition.getReason().equalsIgnoreCase("Cancelled")) {
            jobService.updateJobStatus(jobId, JobStatus.FAILED);
        }
        future.complete(null);
    }

    /**
     * Handles pipeline in running state
     */
    private void handleRunningPipeline(Condition condition) {
        if (shouldSkipDueToLeadership("running status update")) return;

        if ("Unknown".equalsIgnoreCase(condition.getStatus())
                && condition.getReason() != null
                && condition.getReason().contains("Running")) {
            LOGGER.info("PipelineRun {} is running.", pipelineRunName);
            jobService.updateJobStatus(jobId, JobStatus.RUNNING);
        }
    }

    /**
     * Executes operation with standardized error handling
     */
    private void executeWithErrorHandling(Runnable operation, String successMessage, String errorMessage) {
        try {
            operation.run();
            LOGGER.debug(successMessage, jobId);
        } catch (Exception e) {
            LOGGER.error(errorMessage, jobId, e);
        }
    }
}
