package com.redhat.sast.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.redhat.sast.api.common.constants.ApplicationConstants;
import com.redhat.sast.api.enums.InputSourceType;
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

    @Inject
    UrlInferenceService urlInferenceService;

    public JobResponseDto createJob(JobCreationDto jobCreationDto) {
        Job job = createJobInDatabase(jobCreationDto);

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
    public Job createJobInDatabase(JobCreationDto jobCreationDto) {
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

        if (job.getStatus() == JobStatus.RUNNING || job.getStatus() == JobStatus.SCHEDULED) {
            job.setStatus(JobStatus.CANCELLED);
            jobRepository.persist(job);

            // TODO: Implement actual job cancellation logic (e.g., cancel Tekton pipeline)
        } else {
            throw new IllegalStateException("Job cannot be cancelled in status: " + job.getStatus());
        }
    }

    @Transactional
    public void updateJobStatus(@Nonnull Long jobId, @Nonnull JobStatus newStatus) {
        Job job = jobRepository.findById(jobId);
        if (job != null) {
            job.setStatus(newStatus);
            if (newStatus == JobStatus.RUNNING && job.getStartedAt() == null) {
                job.setStartedAt(java.time.LocalDateTime.now());
            } else if (newStatus == JobStatus.COMPLETED || newStatus == JobStatus.FAILED) {
                job.setCompletedAt(java.time.LocalDateTime.now());
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
