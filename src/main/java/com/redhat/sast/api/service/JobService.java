package com.redhat.sast.api.service;

import static com.redhat.sast.api.service.PlatformService.DEFAULT_LLM_SECRET;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    private static final boolean DEFAULT_USE_FALSE_POSITIVE_FILE = true;

    private final JobRepository jobRepository;
    private final JobSettingsRepository jobSettingsRepository;
    private final PlatformService platformService;
    private final ManagedExecutor managedExecutor;
    private final NvrResolutionService nvrResolutionService;
    private final PipelineParameterMapper parameterMapper;

    public JobResponseDto createJob(JobCreationDto jobCreationDto) {
        final Job job = createJobEntity(jobCreationDto);

        final Long jobId = job.getId();
        final List<Param> pipelineParams = parameterMapper.extractPipelineParams(job);
        final String llmSecretName = Optional.ofNullable(job.getJobSettings())
                .map(JobSettings::getSecretName)
                .orElse(DEFAULT_LLM_SECRET);

        managedExecutor.execute(() -> {
            try {
                LOGGER.info("Starting async pipeline execution for job ID: {}", jobId);
                platformService.startSastAIWorkflow(jobId, pipelineParams, llmSecretName);
            } catch (Exception e) {
                handleAsyncJobFailure(jobId, e);
            }
        });

        return convertToResponseDto(job);
    }

    @Transactional
    public Job createJobEntity(JobCreationDto jobCreationDto) {
        Job job = getJobFromDto(jobCreationDto);
        jobRepository.persist(job);

        JobSettings settings = createDefaultJobSettings(job, jobCreationDto);
        jobSettingsRepository.persist(settings);

        job.setJobSettings(settings);
        return job;
    }

    public void cancelJob(@Nonnull Long jobId) {
        final Job job = findJobOrThrow(jobId);
        validateJobForCancellation(job);
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

        final Job job = findJobOrThrow(jobId);

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
        Job job = findJobOrThrow(jobId);
        job.setTektonUrl(tektonUrl);
        jobRepository.persist(job);
        LOGGER.info("Updated job {} with Tekton URL: {}", jobId, tektonUrl);
    }

    public List<JobResponseDto> getAllJobs(String packageName, JobStatus status, int page, int size) {
        return jobRepository.findJobsWithPagination(packageName, status, Page.of(page, size)).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public JobResponseDto getJobDtoByJobId(@Nonnull Long jobId) {
        Job job = findJobOrThrow(jobId);
        return convertToResponseDto(job);
    }

    private JobResponseDto convertToResponseDto(Job job) {
        return JobMapper.INSTANCE.jobToJobResponseDto(job);
    }

    public Job getJobEntityById(@Nonnull Long jobId) {
        return jobRepository.findById(jobId);
    }

    @Transactional
    public void persistJob(@Nonnull Job job) {
        jobRepository.persist(job);
    }

    private Job findJobOrThrow(Long jobId) {
        Job job = jobRepository.findById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found with id: " + jobId);
        }
        return job;
    }

    private void validateJobForCancellation(Job job) {
        JobStatus status = job.getStatus();
        if (!isCancellableStatus(status)) {
            throw new IllegalStateException("Job cannot be cancelled in status: " + status);
        }
    }

    private boolean isCancellableStatus(JobStatus status) {
        return status == JobStatus.RUNNING || status == JobStatus.SCHEDULED || status == JobStatus.PENDING;
    }

    private void handleAsyncJobFailure(Long jobId, Exception e) {
        LOGGER.error("Failed to start pipeline for job ID: {}", jobId, e);
        managedExecutor.execute(() -> {
            try {
                updateJobStatus(jobId, JobStatus.FAILED);
            } catch (Exception updateException) {
                LOGGER.error("Failed to update job status for job ID: {}", jobId, updateException);
            }
        });
    }

    private JobSettings createDefaultJobSettings(Job job, JobCreationDto dto) {
        LOGGER.debug("Creating JobSettings with default secretName: '{}'", ApplicationConstants.DEFAULT_SECRET_NAME);

        JobSettings settings = new JobSettings();
        settings.setJob(job);
        settings.setSecretName(ApplicationConstants.DEFAULT_SECRET_NAME);
        settings.setUseKnownFalsePositiveFile(
                Optional.ofNullable(dto.getUseKnownFalsePositiveFile()).orElse(DEFAULT_USE_FALSE_POSITIVE_FILE));

        LOGGER.debug("Persisted JobSettings with secretName: '{}'", settings.getSecretName());
        return settings;
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

    private Job getJobFromDto(@Nonnull JobCreationDto jobCreationDto) {
        Job job = new Job();

        job.setPackageNvr(jobCreationDto.getPackageNvr());

        // Resolve package information using NVR service
        job.setProjectName(nvrResolutionService.resolveProjectName(jobCreationDto.getPackageNvr()));
        job.setProjectVersion(nvrResolutionService.resolveProjectVersion(jobCreationDto.getPackageNvr()));
        job.setPackageName(nvrResolutionService.resolvePackageName(jobCreationDto.getPackageNvr()));
        job.setPackageSourceCodeUrl(nvrResolutionService.resolveSourceCodeUrl(jobCreationDto.getPackageNvr()));
        job.setKnownFalsePositivesUrl(
                nvrResolutionService.resolveKnownFalsePositivesUrl(jobCreationDto.getPackageNvr()));

        // Set input source - always Google Sheet for now
        job.setInputSourceType(InputSourceType.GOOGLE_SHEET);
        job.setGSheetUrl(jobCreationDto.getInputSourceUrl());

        job.setStatus(JobStatus.PENDING);
        return job;
    }
}
