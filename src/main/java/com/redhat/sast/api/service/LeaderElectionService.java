package com.redhat.sast.api.service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.fabric8.kubernetes.client.extended.leaderelection.LeaderCallbacks;
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderElectionConfigBuilder;
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderElector;
import io.fabric8.kubernetes.client.extended.leaderelection.resourcelock.LeaseLock;
import io.fabric8.tekton.client.TektonClient;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for coordinating leadership across multiple orchestrator pods using Kubernetes leader election.
 *
 * Uses Kubernetes Lease-based leader election to ensure only one pod performs recovery operations
 * at a time. This prevents duplicate watchers and conflicting recovery actions when running with
 * replica=2 or higher.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class LeaderElectionService {

    private final TektonClient tektonClient;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    @ConfigProperty(name = "quarkus.application.name", defaultValue = "sast-ai-orchestrator")
    String applicationName;

    private final AtomicBoolean isLeader = new AtomicBoolean(false);
    private LeaderElector leaderElector;

    /**
     * Attempts to acquire leadership for recovery operations.
     * Uses Kubernetes Lease-based leader election.
     *
     * @param onLeadershipAcquired callback to run when this pod becomes leader
     * @param onLeadershipLost callback to run when this pod loses leadership
     */
    public void runWithLeadership(Runnable onLeadershipAcquired, Runnable onLeadershipLost) {
        String leaseName = applicationName + "-recovery-leader";
        String hostname = System.getenv("HOSTNAME"); // Kubernetes pod name

        final String podName;
        if (hostname == null || hostname.isBlank()) {
            LOGGER.warn("HOSTNAME not set, using fallback identity");
            podName = "orchestrator-" + System.currentTimeMillis();
        } else {
            podName = hostname;
        }

        var kubernetesClient = tektonClient.adapt(io.fabric8.kubernetes.client.KubernetesClient.class);

        var lock = new LeaseLock(namespace, leaseName, podName);

        var callbacks = new LeaderCallbacks(
                () -> {
                    LOGGER.info("Pod {} acquired leadership for recovery", podName);
                    isLeader.set(true);
                    onLeadershipAcquired.run();
                },
                () -> {
                    LOGGER.info("Pod {} lost leadership for recovery", podName);
                    isLeader.set(false);
                    onLeadershipLost.run();
                },
                (identity) -> {
                    LOGGER.warn("Leadership change detected, new leader: {}", identity);
                });

        var config = new LeaderElectionConfigBuilder()
                .withName(leaseName + "-config")
                .withLeaseDuration(Duration.ofSeconds(15))
                .withRenewDeadline(Duration.ofSeconds(10))
                .withRetryPeriod(Duration.ofSeconds(2))
                .withLock(lock)
                .withLeaderCallbacks(callbacks)
                .build();

        leaderElector = kubernetesClient.leaderElector().withConfig(config).build();

        leaderElector.run();
    }

    /**
     * Checks if this pod currently holds leadership.
     *
     * @return true if this pod is the current leader, false otherwise
     */
    public boolean isCurrentlyLeader() {
        return isLeader.get();
    }
}
