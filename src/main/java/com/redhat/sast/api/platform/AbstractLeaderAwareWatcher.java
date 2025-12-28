package com.redhat.sast.api.platform;

import java.util.concurrent.CompletableFuture;

import com.redhat.sast.api.service.LeaderElectionService;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for watchers that need to check leadership before performing operations.
 * Provides a common pattern for aborting work when leadership is lost to prevent
 * duplicate processing across multiple pods.
 */
@Slf4j
public abstract class AbstractLeaderAwareWatcher {

    protected final LeaderElectionService leaderElectionService;
    protected final CompletableFuture<Void> future;
    protected final long jobId;

    protected AbstractLeaderAwareWatcher(
            LeaderElectionService leaderElectionService, CompletableFuture<Void> future, long jobId) {
        this.leaderElectionService = leaderElectionService;
        this.future = future;
        this.jobId = jobId;
    }

    /**
     * Checks if this pod has leadership before processing. If not, completes the future and returns true.
     *
     * @param operationType Description of operation being skipped (for logging)
     * @return true if should skip (not leader), false if should continue (is leader)
     */
    protected boolean shouldSkipDueToLeadership(String operationType) {
        if (!leaderElectionService.isCurrentlyLeader()) {
            LOGGER.info("Not leader, skipping {} for job {}", operationType, jobId);
            future.complete(null);
            return true;
        }
        return false;
    }
}
