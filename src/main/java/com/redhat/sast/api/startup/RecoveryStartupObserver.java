package com.redhat.sast.api.startup;

import java.util.concurrent.CompletableFuture;

import com.redhat.sast.api.service.JobRecoveryService;
import com.redhat.sast.api.service.LeaderElectionService;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Observes application startup and triggers recovery of orphaned jobs and batches.
 *
 * Recovery is always enabled by default and runs immediately after the application starts,
 * ensuring that any jobs or batches orphaned by a previous pod crash are reconciled with their
 * actual Kubernetes PipelineRun status.
 *
 * Uses Kubernetes leader election to ensure only one pod performs recovery when running
 * with replica=2 or higher. The follower pods will do nothing, preventing duplicate watchers
 * and conflicting recovery actions.
 *
 * Priority is set to 2 to ensure this runs after {@link ClusterConnectivityCheck} (which
 * validates cluster connectivity before recovery attempts).
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class RecoveryStartupObserver {

    private final JobRecoveryService recoveryService;
    private final LeaderElectionService leaderElectionService;

    /**
     * Triggers full recovery on application startup using leader election.
     * Only the leader pod will perform recovery, follower pods will do nothing.
     *
     * @param event the startup event
     */
    void onStart(@Observes @Priority(2) StartupEvent event) {
        LOGGER.info("Starting recovery startup observer");

        CompletableFuture.runAsync(() -> {
            leaderElectionService.runWithLeadership(
                    () -> {
                        LOGGER.debug("Leadership acquired, starting job and batch recovery");
                        try {
                            recoveryService.performFullRecovery();
                            LOGGER.info("Startup recovery completed");
                        } catch (Exception e) {
                            LOGGER.error("Startup recovery failed", e);
                        }
                    },
                    () -> {
                        LOGGER.info("Leadership lost, this pod is now follower");
                    });
        });
    }
}
