package com.redhat.sast.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobSettings;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.repository.JobSettingsRepository;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;

import io.quarkus.panache.common.Page;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class JobService {

    private static final Logger LOG = Logger.getLogger(JobService.class);

    @Inject
    JobRepository jobRepository;

    @Inject
    JobSettingsRepository jobSettingsRepository;

    @Inject
    PlatformService platformService;

    @Inject
    ManagedExecutor managedExecutor;

    public JobResponseDto createJob(JobCreationDto jobCreationDto) {
        // First, create the job in the database (transactional)
        Job job = createJobInDatabase(jobCreationDto);

        // Then start the job execution asynchronously (fire and forget)
        managedExecutor.execute(() -> {
            try {
                LOG.infof("Starting async pipeline execution for job ID: %d", job.getId());
                platformService.startSastAIWorkflow(job);
            } catch (Exception e) {
                LOG.errorf(e, "Failed to start pipeline for job ID: %d", job.getId());
                // Update job status to failed if pipeline startup fails
                try {
                    // Use a separate thread to avoid transaction issues
                    managedExecutor.execute(() -> updateJobStatusToFailed(job.getId(), e));
                } catch (Exception updateException) {
                    LOG.errorf(updateException, "Failed to update job status for job ID: %d", job.getId());
                }
            }
        });

        return convertToResponseDto(job);
    }

    @Transactional
    public Job createJobInDatabase(JobCreationDto jobCreationDto) {
        Job job = getJobFromDto(jobCreationDto);
        jobRepository.persist(job);

        // Create job settings if provided
        if (jobCreationDto.getWorkflowSettings() != null) {
            LOG.infof(
                    "Creating JobSettings with secretName: '%s'",
                    jobCreationDto.getWorkflowSettings().getSecretName());

            JobSettings settings = new JobSettings();
            settings.setJob(job);
            settings.setLlmModelName(jobCreationDto.getWorkflowSettings().getLlmModelName());
            settings.setEmbeddingLlmModelName(
                    jobCreationDto.getWorkflowSettings().getEmbeddingsLlmModelName());
            settings.setSecretName(jobCreationDto.getWorkflowSettings().getSecretName());
            jobSettingsRepository.persist(settings);

            // Manually set the JobSettings on the Job object to avoid lazy loading issues
            job.setJobSettings(settings);

            LOG.infof("Persisted JobSettings with secretName: '%s'", settings.getSecretName());
        }

        return job;
    }

    private static Job getJobFromDto(JobCreationDto jobCreationDto) {
        Job job = new Job();

        // Set basic job properties
        job.setProjectName(jobCreationDto.getProjectName());
        job.setProjectVersion(jobCreationDto.getProjectVersion());
        job.setPackageName(jobCreationDto.getPackageName());
        job.setPackageNvr(jobCreationDto.getPackageNvr());
        job.setOshScanId(jobCreationDto.getOshScanId());
        job.setPackageSourceCodeUrl(jobCreationDto.getPackageSourceCodeUrl());
        job.setJiraLink(jobCreationDto.getJiraLink());
        job.setHostname(jobCreationDto.getHostname());
        job.setKnownFalsePositivesUrl(jobCreationDto.getKnownFalsePositivesUrl());

        // Set input source
        if (jobCreationDto.getInputSource() != null) {
            job.setInputSourceType(jobCreationDto.getInputSource().getType());
            job.setGSheetUrl(jobCreationDto.getInputSource().getUrl());
        }

        job.setStatus(JobStatus.PENDING);
        return job;
    }

    public List<JobResponseDto> getAllJobs(String packageName, JobStatus status, int page, int size) {
        return jobRepository.findJobsWithPagination(packageName, status, Page.of(page, size)).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public JobResponseDto getJobById(@Nonnull Long jobId) {
        Job job = jobRepository.findById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found with id: " + jobId);
        }
        return convertToResponseDto(job);
    }

    @Transactional
    public void cancelJob(@Nonnull Long jobId) {
        Job job = jobRepository.findById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found with id: " + jobId);
        }

        if (canBeCancelled(job.getStatus())) {
            markJobAsCancelled(job);

            managedExecutor.execute(() -> {
                try {
                    boolean cancelled = platformService.cancelWorkflow(job);
                    if (!cancelled) {
                        LOG.warnf("Pipeline cancellation unsuccessful for job %d", jobId);
                    }
                } catch (Exception e) {
                    LOG.errorf(e, "Error during pipeline cancellation for job %d", jobId);
                }
            });
        } else {
            throw new IllegalStateException("Job cannot be cancelled in status: " + job.getStatus());
        }
    }

    private boolean canBeCancelled(JobStatus status) {
        return status == JobStatus.RUNNING || status == JobStatus.SCHEDULED || status == JobStatus.PENDING;
    }

    private void markJobAsCancelled(Job job) {
        job.setStatus(JobStatus.CANCELLED);
        job.setCancelledAt(LocalDateTime.now());
        jobRepository.persist(job);
    }

    @Transactional
    public void updateJobStatus(@Nonnull Long jobId, @Nonnull JobStatus newStatus) {

        Job job = jobRepository.findById(jobId);
        if (job == null) {
            LOG.warnf("Job with ID %d not found when trying to update status to %s", jobId, newStatus);
            throw new IllegalArgumentException("Job not found with ID: " + jobId);
        }

        JobStatus currentStatus = job.getStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            LOG.warnf("Invalid status transition from %s to %s for job ID: %d", currentStatus, newStatus, jobId);
            throw new IllegalStateException(String.format(
                    "Invalid status transition from %s to %s for job ID: %d", currentStatus, newStatus, jobId));
        }

        job.setStatus(newStatus);

        switch (newStatus) {
            case RUNNING -> job.setStartedAt(LocalDateTime.now());
            case CANCELLED -> job.setCancelledAt(LocalDateTime.now());
            case COMPLETED, FAILED -> job.setCompletedAt(LocalDateTime.now());
            case PENDING, SCHEDULED -> {
                // No timestamp updates needed for these states
            }
            default -> LOG.warnf("Unhandled job status update: %s for job ID: %d", newStatus, jobId);
        }

        jobRepository.persist(job);
        LOG.debugf("Updated job ID %d status from %s to %s", jobId, currentStatus, newStatus);
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateJobTektonUrl(@Nonnull Long jobId, @Nonnull String tektonUrl) {
        Job job = jobRepository.findById(jobId);
        if (job != null) {
            job.setTektonUrl(tektonUrl);
            jobRepository.persist(job);
            LOG.infof("Updated job %d with Tekton URL: %s", jobId, tektonUrl);
        } else {
            LOG.warnf("Job with ID %d not found when trying to update Tekton URL", jobId);
        }
    }

    private boolean isValidStatusTransition(JobStatus from, JobStatus to) {
        if (from == to) {
            return true; // Same status update considers valid to capture the event timestamp
        }

        return switch (from) {
            case PENDING -> to == JobStatus.SCHEDULED || to == JobStatus.CANCELLED;
            case SCHEDULED -> to == JobStatus.RUNNING || to == JobStatus.CANCELLED;
            case RUNNING -> to == JobStatus.COMPLETED || to == JobStatus.FAILED || to == JobStatus.CANCELLED;
            default -> false;
        };
    }

    private void updateJobStatusToFailed(@Nonnull Long jobId, Exception cause) {
        try {
            // This will use the DataService's REQUIRES_NEW transaction
            // to safely update the job status
            updateJobStatus(jobId, JobStatus.FAILED);
            LOG.infof("Updated job ID %d status to FAILED due to pipeline error: %s", jobId, cause.getMessage());
        } catch (Exception e) {
            LOG.errorf(e, "Critical: Failed to update job status to FAILED for job ID: %d", jobId);
        }
    }

    private JobResponseDto convertToResponseDto(Job job) {
        JobResponseDto dto = new JobResponseDto();
        dto.setJobId(job.getId());
        dto.setProjectName(job.getProjectName());
        dto.setProjectVersion(job.getProjectVersion());
        dto.setPackageName(job.getPackageName());
        dto.setPackageNvr(job.getPackageNvr());
        dto.setOshScanId(job.getOshScanId());
        dto.setSourceCodeUrl(job.getPackageSourceCodeUrl());
        dto.setJiraLink(job.getJiraLink());
        dto.setHostname(job.getHostname());
        dto.setStatus(job.getStatus());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setStartedAt(job.getStartedAt());
        dto.setCompletedAt(job.getCompletedAt());
        dto.setCancelledAt(job.getCancelledAt());
        dto.setTektonUrl(job.getTektonUrl());
        if (job.getJobBatch() != null) {
            dto.setBatchId(job.getJobBatch().getId());
        }
        return dto;
    }

    /**
     * Gets the Job entity by ID for batch processing
     */
    public Job getJobEntityById(@Nonnull Long jobId) {
        return jobRepository.findById(jobId);
    }

    /**
     * Persists a Job entity for batch processing
     */
    @Transactional
    public void persistJob(@Nonnull Job job) {
        jobRepository.persist(job);
    }
}
