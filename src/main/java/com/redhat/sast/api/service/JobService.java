package com.redhat.sast.api.service;

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

        if (!canBeCancelled(job.getStatus())) {
            throw new IllegalStateException("Job cannot be cancelled in status: " + job.getStatus());
        }

        LOG.infof("Cancelling job %d (current status: %s)", jobId, job.getStatus());

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
    }

    private boolean canBeCancelled(JobStatus status) {
        return status == JobStatus.RUNNING || status == JobStatus.SCHEDULED || status == JobStatus.PENDING;
    }

    private void markJobAsCancelled(Job job) {
        job.setStatus(JobStatus.CANCELLED);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        job.setCompletedAt(now);
        job.setCancelledAt(now);
        jobRepository.persist(job);
        LOG.infof("Job %d marked as CANCELLED", job.getId());
    }

    @Transactional
    public void updateJobStatus(@Nonnull Long jobId, @Nonnull JobStatus newStatus) {
        Job job = jobRepository.findById(jobId);
        if (job != null) {
            job.setStatus(newStatus);

            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            switch (newStatus) {
                case RUNNING:
                    if (job.getStartedAt() == null) {
                        job.setStartedAt(now);
                    }
                    break;

                case CANCELLED:
                    if (job.getCancelledAt() == null) {
                        job.setCancelledAt(now);
                    }

                case COMPLETED:
                case FAILED:
                    if (job.getCompletedAt() == null) {
                        job.setCompletedAt(now);
                    }
                    break;

                case PENDING:
                case SCHEDULED:
                    break;

                default:
                    LOG.warnf("Unhandled job status update: %s for job ID: %d", newStatus, jobId);
                    break;
            }

            jobRepository.persist(job);
        }
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

    private void updateJobStatusToFailed(@Nonnull Long jobId, Exception cause) {
        try {
            // This will use the DataService's REQUIRES_NEW transaction
            // to safely update the job status
            updateJobStatus(jobId, JobStatus.FAILED);
            LOG.infof("Updated job ID %d status to FAILED due to pipeline startup failure", jobId);
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
