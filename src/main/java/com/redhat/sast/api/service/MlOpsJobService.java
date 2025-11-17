package com.redhat.sast.api.service;

import java.time.LocalDateTime;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.repository.MlOpsJobRepository;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing MLOps job status and lifecycle.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MlOpsJobService {

    private final MlOpsJobRepository mlOpsJobRepository;

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateJobStatus(@Nonnull Long jobId, @Nonnull JobStatus newStatus) {
        try {
            final MlOpsJob job = mlOpsJobRepository.findById(jobId);
            if (job == null) {
                LOGGER.error("MLOps job with ID {} not found when trying to update status to {}", jobId, newStatus);
                return; // Don't throw, just log and return
            }

            JobStatus currentStatus = job.getStatus();
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                LOGGER.warn(
                        "Invalid status transition from {} to {} for MLOps job ID: {} - allowing it anyway",
                        currentStatus,
                        newStatus,
                        jobId);
                // Don't throw - allow the transition but log the warning
            }

            job.setStatus(newStatus);

            switch (newStatus) {
                case RUNNING -> {
                    if (job.getStartedAt() == null) {
                        job.setStartedAt(LocalDateTime.now());
                    }
                }
                case CANCELLED -> {
                    if (job.getCancelledAt() == null) {
                        job.setCancelledAt(LocalDateTime.now());
                    }
                }
                case COMPLETED, FAILED -> {
                    if (job.getCompletedAt() == null) {
                        job.setCompletedAt(LocalDateTime.now());
                    }
                }
                case PENDING, SCHEDULED -> {
                    // No timestamp updates needed for these states
                }
                default -> LOGGER.warn("Unhandled MLOps job status update: {} for job ID: {}", newStatus, jobId);
            }

            mlOpsJobRepository.persist(job);
            LOGGER.info("Updated MLOps job ID {} status from {} to {}", jobId, currentStatus, newStatus);
        } catch (Exception e) {
            LOGGER.error("Failed to update MLOps job {} status to {}: {}", jobId, newStatus, e.getMessage(), e);
            // Don't rethrow - we don't want to break the watcher
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateJobTektonUrl(@Nonnull Long jobId, @Nonnull String tektonUrl) {
        MlOpsJob job = mlOpsJobRepository.findById(jobId);
        if (job != null) {
            job.setTektonUrl(tektonUrl);
            mlOpsJobRepository.persist(job);
            LOGGER.info("Updated MLOps job {} with Tekton URL: {}", jobId, tektonUrl);
        } else {
            LOGGER.warn("MLOps job with ID {} not found when trying to update Tekton URL", jobId);
        }
    }

    public MlOpsJob getJobEntityById(@Nonnull Long jobId) {
        return mlOpsJobRepository.findById(jobId);
    }

    private boolean isValidStatusTransition(JobStatus from, JobStatus to) {
        if (from == to) {
            return true; // Same status update considered valid to capture the event timestamp
        }

        return switch (from) {
            case PENDING ->
                to == JobStatus.SCHEDULED
                        || to == JobStatus.CANCELLED
                        || to == JobStatus.FAILED
                        || to == JobStatus.RUNNING;
            case SCHEDULED -> to == JobStatus.RUNNING || to == JobStatus.CANCELLED || to == JobStatus.FAILED;
            case RUNNING -> to == JobStatus.COMPLETED || to == JobStatus.FAILED || to == JobStatus.CANCELLED;
            default -> false;
        };
    }
}
