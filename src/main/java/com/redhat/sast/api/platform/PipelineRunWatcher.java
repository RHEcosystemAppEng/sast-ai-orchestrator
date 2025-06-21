package com.redhat.sast.api.platform;

import java.util.concurrent.CompletableFuture;

import org.jboss.logging.Logger;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.service.JobService;

import io.fabric8.knative.pkg.apis.Condition;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.tekton.v1.PipelineRun;

/**
 * Watcher implementation for monitoring Tekton PipelineRun status changes.
 * Updates job status in the database based on pipeline execution results.
 */
public class PipelineRunWatcher implements Watcher<PipelineRun> {

    private static final Logger LOG = Logger.getLogger(PipelineRunWatcher.class);
    private static final String SUCCEEDED_CONDITION = "Succeeded";
    private static final String STATUS_TRUE = "True";
    private static final String STATUS_FALSE = "False";

    private final String pipelineRunName;
    private final long jobId;
    private final CompletableFuture<Void> future;
    private final JobService jobService;

    public PipelineRunWatcher(
            String pipelineRunName, long jobId, CompletableFuture<Void> future, JobService jobService) {
        this.pipelineRunName = pipelineRunName;
        this.jobId = jobId;
        this.future = future;
        this.jobService = jobService;
    }

    @Override
    public void eventReceived(Action action, PipelineRun resource) {
        LOG.infof(
                "Watcher event: %s for PipelineRun: %s",
                action, resource.getMetadata().getName());

        if (resource.getStatus() != null && resource.getStatus().getConditions() != null) {
            for (Condition condition : resource.getStatus().getConditions()) {
                if (SUCCEEDED_CONDITION.equals(condition.getType())) {
                    if (STATUS_TRUE.equalsIgnoreCase(condition.getStatus())) {
                        LOG.infof("PipelineRun %s succeeded.", pipelineRunName);
                        jobService.updateJobStatus(jobId, JobStatus.COMPLETED);
                        future.complete(null);
                        return;
                    } else if (STATUS_FALSE.equalsIgnoreCase(condition.getStatus())) {
                        LOG.errorf(
                                "PipelineRun %s failed. Reason: %s, Message: %s",
                                pipelineRunName, condition.getReason(), condition.getMessage());
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
            LOG.errorf(cause, "Watcher for %s closed due to an error.", pipelineRunName);
            future.completeExceptionally(cause);
        } else {
            LOG.warnf("Watcher for %s closed cleanly without a final status.", pipelineRunName);
            future.complete(null);
        }
    }
}
