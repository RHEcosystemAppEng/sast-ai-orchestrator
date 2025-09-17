package com.redhat.sast.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.redhat.sast.api.common.constants.ApplicationConstants;
import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.mapper.JobMapper;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.JobSettings;
import com.redhat.sast.api.platform.PipelineParameterMapper;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.repository.JobSettingsRepository;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;

import io.fabric8.tekton.v1.Param;
import io.quarkus.panache.common.Page;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final JobSettingsRepository jobSettingsRepository;
    private final PlatformService platformService;
    private final ManagedExecutor managedExecutor;
    private final NvrResolutionService nvrResolutionService;
    private final PipelineParameterMapper parameterMapper;
    private final UrlValidationService urlValidationService;

    public JobResponseDto createJob(JobCreationDto jobCreationDto) {
        final Job job = createJobEntity(jobCreationDto);

        final Long jobId = job.getId();
        final List<Param> pipelineParams = parameterMapper.extractPipelineParams(job);
        final String llmSecretName =
                (job.getJobSettings() != null) ? job.getJobSettings().getSecretName() : "sast-ai-default-llm-creds";

        managedExecutor.execute(() -> {
            try {
                LOGGER.info("Starting async pipeline execution for job ID: {}", jobId);
                platformService.startSastAIWorkflow(jobId, pipelineParams, llmSecretName);
            } catch (Exception e) {
                LOGGER.error("Failed to start pipeline for job ID: {}", jobId, e);
                try {
                    managedExecutor.execute(() -> updateJobStatusToFailed(jobId, e));
                } catch (Exception updateException) {
                    LOGGER.error("Failed to update job status for job ID: {}", jobId, updateException);
                }
            }
        });

        return convertToResponseDto(job);
    }

    public void cancelJob(@Nonnull Long jobId) {
        final Job job = jobRepository.findById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be found with id: " + jobId);
        }

        JobStatus status = job.getStatus();
        if (!(status == JobStatus.RUNNING || status == JobStatus.SCHEDULED || status == JobStatus.PENDING)) {
            throw new IllegalStateException("Job cannot be cancelled in status: " + job.getStatus());
        }
        String tektonUrl = job.getTektonUrl();

        updateJobStatus(jobId, JobStatus.CANCELLED);
        managedExecutor.execute(() -> {
            try {
                boolean cancelled = platformService.cancelTektonPipelineRun(tektonUrl, jobId);
                if (!cancelled) {
                    LOGGER.warn("Pipeline cancellation unsuccessful for job {}", jobId);
                }
            } catch (Exception e) {
                LOGGER.error("Error during job cancellation for job {}", jobId, e);
            }
        });
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateJobStatus(@Nonnull Long jobId, @Nonnull JobStatus newStatus) {

        final Job job = jobRepository.findById(jobId);
        if (job == null) {
            LOGGER.warn("Job with ID {} not found when trying to update status to {}", jobId, newStatus);
            throw new IllegalArgumentException("Job not found with ID: " + jobId);
        }

        JobStatus currentStatus = job.getStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            LOGGER.warn("Invalid status transition from {} to {} for job ID: {}", currentStatus, newStatus, jobId);
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
            default -> LOGGER.warn("Unhandled job status update: {} for job ID: {}", newStatus, jobId);
        }

        jobRepository.persist(job);
        LOGGER.debug("Updated job ID {} status from {} to {}", jobId, currentStatus, newStatus);
    }

    @Transactional
    public void updateJobTektonUrl(@Nonnull Long jobId, @Nonnull String tektonUrl) {
        Job job = jobRepository.findById(jobId);
        if (job != null) {
            job.setTektonUrl(tektonUrl);
            jobRepository.persist(job);
            LOGGER.info("Updated job {} with Tekton URL: {}", jobId, tektonUrl);
        } else {
            LOGGER.warn("Job with ID {} not found when trying to update Tekton URL", jobId);
        }
    }

    @Transactional
    public Job createJobEntity(JobCreationDto jobCreationDto) {
        Job job = getJobFromDto(jobCreationDto);
        jobRepository.persist(job);

        LOGGER.debug("Creating JobSettings with default secretName: '{}'", ApplicationConstants.DEFAULT_SECRET_NAME);

        JobSettings settings = new JobSettings();
        settings.setJob(job);
        settings.setSecretName(ApplicationConstants.DEFAULT_SECRET_NAME);

        boolean shouldUseFile = shouldUseFalsePositiveFile(job, jobCreationDto);

        settings.setUseKnownFalsePositiveFile(shouldUseFile);
        jobSettingsRepository.persist(settings);

        job.setJobSettings(settings);

        return job;
    }

    private Job getJobFromDto(JobCreationDto jobCreationDto) {
        Job job = new Job();

        job.setPackageNvr(jobCreationDto.getPackageNvr());

        job.setProjectName(nvrResolutionService.resolveProjectName(jobCreationDto.getPackageNvr()));
        job.setProjectVersion(nvrResolutionService.resolveProjectVersion(jobCreationDto.getPackageNvr()));
        job.setPackageName(nvrResolutionService.resolvePackageName(jobCreationDto.getPackageNvr()));
        job.setPackageSourceCodeUrl(nvrResolutionService.resolveSourceCodeUrl(jobCreationDto.getPackageNvr()));
        job.setKnownFalsePositivesUrl(
                nvrResolutionService.resolveKnownFalsePositivesUrl(jobCreationDto.getPackageNvr()));

        // Set input source - always Google Sheet for now
        job.setInputSourceType(InputSourceType.GOOGLE_SHEET);
        job.setGSheetUrl(jobCreationDto.getInputSourceUrl());

        // Set submittedBy with default value "unknown" if not provided
        job.setSubmittedBy(jobCreationDto.getSubmittedBy() != null ? jobCreationDto.getSubmittedBy() : "unknown");

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

    private void updateJobStatusToFailed(@Nonnull Long jobId, Exception cause) {
        try {
            // This will use the DataService's REQUIRES_NEW transaction
            // to safely update the job status
            updateJobStatus(jobId, JobStatus.FAILED);
            LOGGER.info("Updated job ID {} status to FAILED due to pipeline error: {}", jobId, cause.getMessage());
        } catch (Exception e) {
            LOGGER.error("Critical: Failed to update job status to FAILED for job ID: {}", jobId, e);
        }
    }

    private boolean shouldUseFalsePositiveFile(Job job, JobCreationDto jobCreationDto) {
        Boolean useFromDto = jobCreationDto.getUseKnownFalsePositiveFile();
        boolean defaultToUse = useFromDto != null ? useFromDto : true;

        if (!defaultToUse || job.getKnownFalsePositivesUrl() == null) {
            return defaultToUse;
        }

        if (urlValidationService.isUrlAccessible(job.getKnownFalsePositivesUrl())) {
            return true;
        }

        LOGGER.info(
                "Known false positives file not found for package '{}' at URL: {}. Setting useKnownFalsePositiveFile to false.",
                job.getPackageNvr(),
                job.getKnownFalsePositivesUrl());
        return false;
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

    /**
     * Updates DVC metadata fields for a job
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateJobDvcMetadata(
            @Nonnull Long jobId, String dvcDataVersion, String dvcCommitHash, String dvcPipelineStage) {
        final Job job = jobRepository.findById(jobId);
        if (job == null) {
            LOGGER.warn("Job with ID {} not found when trying to update DVC metadata", jobId);
            throw new IllegalArgumentException("Job not found with ID: " + jobId);
        }

        job.setDvcDataVersion(dvcDataVersion);
        job.setDvcCommitHash(dvcCommitHash);
        job.setDvcPipelineStage(dvcPipelineStage);
        job.setLastUpdatedAt(LocalDateTime.now());

        LOGGER.debug(
                "Updated DVC metadata for job ID {}: version={}, commit={}, stage={}",
                jobId,
                dvcDataVersion,
                dvcCommitHash,
                dvcPipelineStage);
    }
}
