package com.redhat.sast.api.platform;

import java.util.concurrent.CompletableFuture;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.service.JobService;

import io.fabric8.knative.pkg.apis.Condition;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.tekton.v1.PipelineRun;
import lombok.extern.slf4j.Slf4j;

/**
 * Watcher implementation for monitoring Tekton PipelineRun status changes.
 * Updates job status in the database based on pipeline execution results.
 */
@Slf4j
public class PipelineRunWatcher implements Watcher<PipelineRun> {

    private static final String SUCCEEDED_CONDITION = "Succeeded";
    private static final String STATUS_TRUE = "True";
    private static final String STATUS_FALSE = "False";

    private final String pipelineRunName;
    private final long jobId;
    private final CompletableFuture<Void> future;
    private final JobService jobService;
    private final KubernetesResourceManager resourceManager;

    public PipelineRunWatcher(
            String pipelineRunName,
            long jobId,
            CompletableFuture<Void> future,
            JobService jobService,
            KubernetesResourceManager resourceManager) {
        this.pipelineRunName = pipelineRunName;
        this.jobId = jobId;
        this.future = future;
        this.jobService = jobService;
        this.resourceManager = resourceManager;
    }

    @Override
    public void eventReceived(Action action, PipelineRun resource) {
        LOGGER.info(
                "Watcher event: {} for PipelineRun: {}",
                action,
                resource.getMetadata().getName());

        if (action == Action.DELETED) {
            LOGGER.info("PipelineRun {} was deleted (likely cancelled)", pipelineRunName);
            // Handle pipeline deletion in a separate transactional context
            try {
                resourceManager.handlePipelineDeletion(jobId);
            } catch (Exception e) {
                LOGGER.error("Failed to handle pipeline deletion for job {}", jobId, e);
            }
            future.complete(null);
            return;
        }

        if (resource.getStatus() != null && resource.getStatus().getConditions() != null) {
            for (Condition condition : resource.getStatus().getConditions()) {
                if (SUCCEEDED_CONDITION.equals(condition.getType())) {
                    if (STATUS_TRUE.equalsIgnoreCase(condition.getStatus())) {
                        LOGGER.info("PipelineRun {} succeeded.", pipelineRunName);
                        jobService.updateJobStatus(jobId, JobStatus.COMPLETED);
                        future.complete(null);
                        return;
                    } else if (STATUS_FALSE.equalsIgnoreCase(condition.getStatus())) {
                        LOGGER.error(
                                "PipelineRun {} failed. Reason: {}, Message: {}",
                                pipelineRunName,
                                condition.getReason(),
                                condition.getMessage());
                        jobService.updateJobStatus(jobId, JobStatus.FAILED);
                        future.complete(null);
                        return;
                    }
                }
            }
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
}
