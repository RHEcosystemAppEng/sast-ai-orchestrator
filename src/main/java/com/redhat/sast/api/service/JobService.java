package com.redhat.sast.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.redhat.sast.api.common.constants.ApplicationConstants;
import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.mapper.JobMapper;
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

    @Inject
    UrlInferenceService urlInferenceService;

    public JobResponseDto createJob(JobCreationDto jobCreationDto) {
        // First, create the job entity (transactional)
        Job job = createJobEntity(jobCreationDto);

        managedExecutor.execute(() -> {
            try {
                LOG.infof("Starting async pipeline execution for job ID: %d", job.getId());
                platformService.startSastAIWorkflow(job);
            } catch (Exception e) {
                LOG.errorf(e, "Failed to start pipeline for job ID: %d", job.getId());
                try {
                    managedExecutor.execute(() -> updateJobStatusToFailed(job.getId(), e));
                } catch (Exception updateException) {
                    LOG.errorf(updateException, "Failed to update job status for job ID: %d", job.getId());
                }
            }
        });

        return convertToResponseDto(job);
    }

    @Transactional
    public void cancelJob(@Nonnull Long jobId) {
        Job job = jobRepository.findById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be found with id: " + jobId);
        }

        JobStatus status = job.getStatus();
        if (status == JobStatus.RUNNING || status == JobStatus.SCHEDULED || status == JobStatus.PENDING) {
            updateJobStatus(jobId, JobStatus.CANCELLED);

            managedExecutor.execute(() -> {
                try {
                    boolean cancelled = platformService.cancelTektonPipelineRun(job);
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

    @Transactional
    public Job createJobEntity(JobCreationDto jobCreationDto) {
        Job job = getJobFromDto(jobCreationDto);
        jobRepository.persist(job);

        LOG.debugf("Creating JobSettings with default secretName: '%s'", ApplicationConstants.DEFAULT_SECRET_NAME);

        JobSettings settings = new JobSettings();
        settings.setJob(job);
        settings.setSecretName(ApplicationConstants.DEFAULT_SECRET_NAME);
        Boolean useKnownFalsePositiveFile = jobCreationDto.getUseKnownFalsePositiveFile();
        settings.setUseKnownFalsePositiveFile(useKnownFalsePositiveFile != null ? useKnownFalsePositiveFile : true);
        jobSettingsRepository.persist(settings);

        job.setJobSettings(settings);

        LOG.debugf("Persisted JobSettings with secretName: '%s'", settings.getSecretName());

        return job;
    }

    private Job getJobFromDto(JobCreationDto jobCreationDto) {
        Job job = new Job();

        job.setPackageNvr(jobCreationDto.getPackageNvr());

        job.setProjectName(urlInferenceService.inferProjectName(jobCreationDto.getPackageNvr()));
        job.setProjectVersion(urlInferenceService.inferProjectVersion(jobCreationDto.getPackageNvr()));
        job.setPackageName(urlInferenceService.inferPackageName(jobCreationDto.getPackageNvr()));
        job.setPackageSourceCodeUrl(urlInferenceService.inferSourceCodeUrl(jobCreationDto.getPackageNvr()));
        job.setKnownFalsePositivesUrl(urlInferenceService.inferKnownFalsePositivesUrl(jobCreationDto.getPackageNvr()));

        // Set input source - always Google Sheet for now
        job.setInputSourceType(InputSourceType.GOOGLE_SHEET);
        job.setGSheetUrl(jobCreationDto.getInputSourceUrl());

        job.setStatus(JobStatus.PENDING);
        return job;
    }

    public List<JobResponseDto> getAllJobs(String packageName, JobStatus status, int page, int size) {
        return jobRepository.findJobsWithPagination(packageName, status, Page.of(page, size)).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public JobResponseDto getJobDtoByJobId(@Nonnull Long jobId) {
        Job job = jobRepository.findById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found with id: " + jobId);
        }
        return convertToResponseDto(job);
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
        return JobMapper.INSTANCE.jobToJobResponseDto(job);
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
