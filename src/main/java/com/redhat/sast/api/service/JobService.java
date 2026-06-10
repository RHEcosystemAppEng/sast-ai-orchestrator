package com.redhat.sast.api.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
    private final EventBroadcastService eventBroadcastService;

    public JobResponseDto createJob(JobCreationDto jobCreationDto) {
        validateAndPrepare(jobCreationDto);

        String packageNvr = jobCreationDto.getPackageNvr();
        boolean forceRescan = jobCreationDto.getForceRescan();

        InputSourceType inputSourceType = determineInputSourceType(jobCreationDto);

        // Check for existing scans only if not forcing a rescan
        if (forceRescan) {
            LOGGER.info("Force rescan requested for NVR: {}, bypassing cache check", packageNvr);
        } else {
            // Check for existing completed scan (return cached result)
            var cachedJobResult = checkForCompletedScan(packageNvr, inputSourceType);
            if (cachedJobResult != null) {
                return cachedJobResult;
            }

            // Check for existing running/pending scan (return existing job info)
            var existingRunResult = checkForActiveScan(packageNvr, inputSourceType);
            if (existingRunResult != null) {
                return existingRunResult;
            }
        }
        // No cached result found or force rescan requested - create new job
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

    private void validateAndPrepare(JobCreationDto dto) {
        boolean hasSourceCodeUrl = StringUtils.isNotBlank(dto.getSourceCodeUrl());
        boolean hasCommitId = StringUtils.isNotBlank(dto.getCommitId());
        boolean hasInputSourceUrl = StringUtils.isNotBlank(dto.getInputSourceUrl());
        boolean hasGitRevision = StringUtils.isNotBlank(dto.getGitRevision());

        if (hasSourceCodeUrl) {
            if (hasCommitId) {
                ensurePackageNvr(dto, dto.getSourceCodeUrl(), dto.getCommitId());
                return;
            }
            throw new IllegalArgumentException("commitId is required when sourceCodeUrl is provided");
        }
        if (hasInputSourceUrl && hasGitRevision) {
            ensurePackageNvr(dto, dto.getInputSourceUrl(), dto.getGitRevision());
            return;
        }
        if (StringUtils.isBlank(dto.getPackageNvr())) {
            throw new IllegalArgumentException(
                    "Either packageNvr, sourceCodeUrl with commitId, or inputSourceUrl with gitRevision must be provided");
        }
    }

    private void ensurePackageNvr(@Nonnull JobCreationDto dto, @Nonnull String repoUrl, @Nonnull String revision) {
        if (StringUtils.isNotBlank(dto.getPackageNvr())) {
            return;
        }
        String orgRepo = extractOrgAndRepo(repoUrl);
        String shortRevision = revision.substring(0, Math.min(7, revision.length()));
        dto.setPackageNvr(orgRepo + "-" + shortRevision);
    }

    private String extractOrgAndRepo(@Nonnull String sourceCodeUrl) {
        String path = sourceCodeUrl.trim().replaceFirst("(https?|git)://[^/]+/", "");
        if (path.endsWith(".git")) {
            path = path.substring(0, path.length() - 4);
        }
        return path;
    }

    /**
     * Determines the input source type based on the request parameters.
     * - Konflux scans have sarifUri
     * - OSH scans have both oshScanId and inputSourceUrl
     * - Google Sheets/SARIF only have inputSourceUrl
     */
    private InputSourceType determineInputSourceType(JobCreationDto jobCreationDto) {
        if (ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK.test(jobCreationDto.getSarifUri())) {
            return InputSourceType.KONFLUX_SCAN;
        }
        if (ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK.test(jobCreationDto.getOshScanId())
                && ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK.test(jobCreationDto.getInputSourceUrl())) {
            return InputSourceType.OSH_SCAN;
        }
        return InputSourceType.GOOGLE_SHEET;
    }

    /**
     * Checks if there's a completed scan for the given NVR and input source type.
     * Duplicate detection is based on NVR + InputSourceType:
     * - Same NVR + same source type (OSH or GSheet) = duplicate → return cached
     * - Same NVR + different source type = NOT duplicate → allow new scan
     *
     * Returns cached result if found, null otherwise.
     */
    private JobResponseDto checkForCompletedScan(String packageNvr, InputSourceType inputSourceType) {
        return jobRepository
                .findCompletedByNvrAndInputSourceType(packageNvr, inputSourceType)
                .map(completedJob -> {
                    LOGGER.info(
                            "Found existing completed {} scan for NVR: {} (job ID: {}), returning cached result",
                            inputSourceType,
                            packageNvr,
                            completedJob.getId());
                    return JobMapper.INSTANCE.jobToJobResponseDto(completedJob, true, false);
                })
                .orElse(null);
    }

    /**
     * Checks if there's an active (running, pending, or scheduled) scan for the given NVR and input source type.
     * Returns existing job info if found, null otherwise.
     */
    private JobResponseDto checkForActiveScan(String packageNvr, InputSourceType inputSourceType) {
        // Check statuses in order of priority: RUNNING, PENDING, SCHEDULED
        for (JobStatus status : List.of(JobStatus.RUNNING, JobStatus.PENDING, JobStatus.SCHEDULED)) {
            List<Job> activeJobs = jobRepository.findByNvrInputSourceTypeAndStatus(packageNvr, inputSourceType, status);

            if (!activeJobs.isEmpty()) {
                Job activeJob = activeJobs.get(0);
                LOGGER.info(
                        "Found existing {} {} scan for NVR: {} (job ID: {}), returning job info",
                        status,
                        inputSourceType,
                        packageNvr,
                        activeJob.getId());
                return JobMapper.INSTANCE.jobToJobResponseDto(activeJob, false, true);
            }
        }

        return null;
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
            case RUNNING -> job.setStartedAt(Instant.now());
            case CANCELLED -> job.setCancelledAt(Instant.now());
            case COMPLETED, FAILED -> job.setCompletedAt(Instant.now());
            case PENDING, SCHEDULED -> {
                // No timestamp updates needed for these states
            }
            default -> LOGGER.warn("Unhandled job status update: {} for job ID: {}", newStatus, jobId);
        }

        jobRepository.persist(job);
        LOGGER.debug("Updated job ID {} status from {} to {}", jobId, currentStatus, newStatus);

        eventBroadcastService.broadcastJobStatusChange(job);
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
    public Job createJobEntity(@Nonnull final JobCreationDto jobCreationDto) {
        Job job = getJobFromDto(jobCreationDto);
        jobRepository.persist(job);

        LOGGER.debug("Creating JobSettings with default secretName: '{}'", ApplicationConstants.DEFAULT_SECRET_NAME);

        JobSettings settings = new JobSettings();
        settings.setJob(job);
        settings.setSecretName(ApplicationConstants.DEFAULT_SECRET_NAME);

        boolean shouldUseFile = shouldUseFalsePositiveFile(jobCreationDto);

        settings.setUseKnownFalsePositiveFile(shouldUseFile);
        jobSettingsRepository.persist(settings);

        job.setJobSettings(settings);

        return job;
    }

    private Job getJobFromDto(JobCreationDto dto) {
        Job job = new Job();
        job.setPackageNvr(dto.getPackageNvr());

        if (StringUtils.isNotBlank(dto.getSourceCodeUrl())) {
            setProjectFieldsFromUrl(job, dto.getSourceCodeUrl(), dto.getCommitId());
        } else if (StringUtils.isNotBlank(dto.getSarifUri()) && StringUtils.isNotBlank(dto.getInputSourceUrl())) {
            setProjectFieldsFromUrl(job, dto.getInputSourceUrl(), dto.getGitRevision());
        } else {
            String nvr = dto.getPackageNvr();
            job.setProjectName(nvrResolutionService.resolveProjectName(nvr));
            job.setProjectVersion(nvrResolutionService.resolveProjectVersion(nvr));
            job.setPackageName(nvrResolutionService.resolvePackageName(nvr));
        }

        configureInputSource(job, dto);
        job.setSubmittedBy(dto.getSubmittedBy() != null ? dto.getSubmittedBy() : "unknown");
        job.setAggregateResultsGSheet(dto.getAggregateResultsGSheet());
        job.setStatus(JobStatus.PENDING);
        return job;
    }

    private void setProjectFieldsFromUrl(Job job, String repoUrl, String revision) {
        String repoName = extractRepoName(repoUrl);
        String shortRevision = revision != null ? revision.substring(0, Math.min(7, revision.length())) : "";
        job.setProjectName(repoName);
        job.setProjectVersion(shortRevision);
        job.setPackageName(repoName);
    }

    private String extractRepoName(String sourceCodeUrl) {
        String path = extractOrgAndRepo(sourceCodeUrl);
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    /**
     * Configures the job's input source type and related fields based on the JobCreationDto content.
     * Handles Konflux scans (sarifUri-based), OSH scan jobs (URL-based) and Google Sheet jobs.
     */
    private void configureInputSource(Job job, JobCreationDto jobCreationDto) {
        String oshScanId = jobCreationDto.getOshScanId();
        String inputSourceUrl = jobCreationDto.getInputSourceUrl();
        String sarifUri = jobCreationDto.getSarifUri();

        // Konflux scan with SARIF URI
        if (ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK.test(sarifUri)) {
            job.setInputSourceType(InputSourceType.KONFLUX_SCAN);

            // For KONFLUX_SCAN, use inputSourceUrl as git URL and set git revision
            job.setPackageSourceCodeUrl(jobCreationDto.getInputSourceUrl());
            job.setGitRevision(jobCreationDto.getGitRevision());
            job.setSarifUri(jobCreationDto.getSarifUri());

            LOGGER.debug(
                    "Configured job as KONFLUX_SCAN - sarifUri: {}, gitRevision: {}, package NVR: {}",
                    sarifUri,
                    jobCreationDto.getGitRevision(),
                    job.getPackageNvr());
            return;
        }

        // OSH scan with URL
        if (oshScanId != null
                && !oshScanId.trim().isEmpty()
                && inputSourceUrl != null
                && !inputSourceUrl.trim().isEmpty()) {
            job.setInputSourceType(InputSourceType.OSH_SCAN);
            job.setOshScanId(oshScanId);
            job.setGSheetUrl(inputSourceUrl);

            // Resolve package source and known FP URLs using NVR
            job.setPackageSourceCodeUrl(nvrResolutionService.resolveSourceCodeUrl(jobCreationDto.getPackageNvr()));
            job.setKnownFalsePositivesUrl(
                    nvrResolutionService.resolveKnownFalsePositivesUrl(jobCreationDto.getPackageNvr()));

            LOGGER.debug(
                    "Configured job as OSH_SCAN - oshScanId: {}, OSH report URL: {}, package source URL: {}",
                    oshScanId,
                    inputSourceUrl,
                    job.getPackageSourceCodeUrl());
            return;
        }

        // Google Sheets or SARIF
        if (ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK.test(inputSourceUrl)) {
            job.setInputSourceType(InputSourceType.GOOGLE_SHEET);
            job.setGSheetUrl(inputSourceUrl);

            if (ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK.test(jobCreationDto.getSourceCodeUrl())) {
                job.setPackageSourceCodeUrl(jobCreationDto.getSourceCodeUrl());
                job.setGitRevision(jobCreationDto.getCommitId());
                LOGGER.debug(
                        "Configured job as GOOGLE_SHEET with sourceCodeUrl: {}, commitId: {}",
                        jobCreationDto.getSourceCodeUrl(),
                        jobCreationDto.getCommitId());
            } else {
                job.setPackageSourceCodeUrl(nvrResolutionService.resolveSourceCodeUrl(jobCreationDto.getPackageNvr()));
                job.setKnownFalsePositivesUrl(
                        nvrResolutionService.resolveKnownFalsePositivesUrl(jobCreationDto.getPackageNvr()));
                LOGGER.debug("Configured job as GOOGLE_SHEET with URL: {}", inputSourceUrl);
            }
            return;
        }

        throw new IllegalArgumentException(
                "Job creation requires either sarifUri for Konflux scans, (oshScanId + inputSourceUrl) for OSH scans, or inputSourceUrl for Google Sheets. "
                        + "Package NVR: " + jobCreationDto.getPackageNvr());
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

    private boolean shouldUseFalsePositiveFile(JobCreationDto dto) {
        if (StringUtils.isNotBlank(dto.getSourceCodeUrl())) {
            return false;
        }
        return !Boolean.FALSE.equals(dto.getUseKnownFalsePositiveFile());
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
        job.setLastUpdatedAt(Instant.now());

        jobRepository.persist(job);

        LOGGER.debug(
                "Updated DVC metadata for job ID {}: version={}, commit={}, stage={}",
                jobId,
                dvcDataVersion,
                dvcCommitHash,
                dvcPipelineStage);
    }
}
