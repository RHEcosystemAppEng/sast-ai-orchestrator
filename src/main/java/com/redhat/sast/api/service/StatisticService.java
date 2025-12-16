package com.redhat.sast.api.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.redhat.sast.api.config.OshConfiguration;
import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.enums.TimePeriod;
import com.redhat.sast.api.mapper.JobMapper;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.OshUncollectedScan;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.repository.osh.OshUncollectedScanRepository;
import com.redhat.sast.api.v1.dto.response.DashboardSummaryDto;
import com.redhat.sast.api.v1.dto.response.JobActivityDataPointDto;
import com.redhat.sast.api.v1.dto.response.OshScanStatusDto;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for generating summary statistics and OSH scan queries.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class StatisticService {

    private static final String STATUS_FIELD = "status";
    private static final String STATUS_COLLECTED = "COLLECTED";
    private static final String STATUS_UNCOLLECTED = "UNCOLLECTED";
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final JobRepository jobRepository;
    private final JobBatchRepository jobBatchRepository;
    private final OshUncollectedScanRepository oshUncollectedScanRepository;
    private final OshConfiguration oshConfiguration;

    /**
     * Gets dashboard summary statistics for a specific time period.
     *
     * @param timePeriod the time period to filter by (null = all time)
     * @return aggregated dashboard statistics for the time period
     */
    public DashboardSummaryDto getSummary(TimePeriod timePeriod) {
        return calculateSummary(timePeriod);
    }

    private DashboardSummaryDto calculateSummary(TimePeriod timePeriod) {
        DashboardSummaryDto dto = new DashboardSummaryDto();

        if (timePeriod == null) {
            // No time filtering - count all records
            // Job statistics
            dto.setTotalJobs(jobRepository.count());
            dto.setPendingJobs(jobRepository.count(STATUS_FIELD, JobStatus.PENDING));
            dto.setRunningJobs(jobRepository.count(STATUS_FIELD, JobStatus.RUNNING));
            dto.setCompletedJobs(jobRepository.count(STATUS_FIELD, JobStatus.COMPLETED));
            dto.setFailedJobs(jobRepository.count(STATUS_FIELD, JobStatus.FAILED));
            dto.setCancelledJobs(jobRepository.count(STATUS_FIELD, JobStatus.CANCELLED));

            // Batch statistics
            dto.setTotalBatches(jobBatchRepository.count());
            dto.setProcessingBatches(jobBatchRepository.count(STATUS_FIELD, BatchStatus.PROCESSING));
            dto.setCompletedBatches(jobBatchRepository.count(STATUS_FIELD, BatchStatus.COMPLETED));

            // OSH scan statistics
            long collectedOshScans = jobRepository.countJobsWithOshScanId();
            long uncollectedOshScans = oshUncollectedScanRepository.count();

            dto.setCollectedOshScans(collectedOshScans);
            dto.setUncollectedOshScans(uncollectedOshScans);
            dto.setTotalOshScans(collectedOshScans + uncollectedOshScans);
        } else {
            // Time filtering - use time range queries
            Instant now = Instant.now();
            Instant startTime = now.minusSeconds(timePeriod.getTotalSeconds());
            Instant endTime = now;

            // Job statistics (filtered by createdAt)
            dto.setTotalJobs(jobRepository.countInTimeRange(startTime, endTime));
            dto.setPendingJobs(jobRepository.countByStatusInTimeRange(JobStatus.PENDING, startTime, endTime));
            dto.setRunningJobs(jobRepository.countByStatusInTimeRange(JobStatus.RUNNING, startTime, endTime));
            dto.setCompletedJobs(jobRepository.countByStatusInTimeRange(JobStatus.COMPLETED, startTime, endTime));
            dto.setFailedJobs(jobRepository.countByStatusInTimeRange(JobStatus.FAILED, startTime, endTime));
            dto.setCancelledJobs(jobRepository.countByStatusInTimeRange(JobStatus.CANCELLED, startTime, endTime));

            // Batch statistics (filtered by submittedAt)
            dto.setTotalBatches(jobBatchRepository.countInTimeRange(startTime, endTime));
            dto.setProcessingBatches(
                    jobBatchRepository.countByStatusInTimeRange(BatchStatus.PROCESSING, startTime, endTime));
            dto.setCompletedBatches(
                    jobBatchRepository.countByStatusInTimeRange(BatchStatus.COMPLETED, startTime, endTime));

            // OSH scan statistics (filtered by createdAt)
            long collectedOshScans = jobRepository.countJobsWithOshScanIdInTimeRange(startTime, endTime);
            long uncollectedOshScans = oshUncollectedScanRepository.countInTimeRange(startTime, endTime);

            dto.setCollectedOshScans(collectedOshScans);
            dto.setUncollectedOshScans(uncollectedOshScans);
            dto.setTotalOshScans(collectedOshScans + uncollectedOshScans);
        }

        dto.setTimestamp(Instant.now());

        return dto;
    }

    /**
     * Retrieves all OSH scans (both collected and uncollected) with pagination and filtering.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @param status filter by status: "COLLECTED", "UNCOLLECTED", or null for all
     * @return list of OSH scans with associated job and retry information
     */
    public List<OshScanStatusDto> getAllOshScans(Integer page, Integer size, String status) {
        page = (page != null && page >= 0) ? page : 0;
        size = (size != null && size > 0) ? size : DEFAULT_PAGE_SIZE;

        List<OshScanStatusDto> results = new ArrayList<>();

        boolean includeCollected = (status == null || STATUS_COLLECTED.equals(status));
        boolean includeUncollected = (status == null || STATUS_UNCOLLECTED.equals(status));

        if (includeCollected) {
            List<OshScanStatusDto> collectedScans = getCollectedScans(page, size);
            results.addAll(collectedScans);
        }

        if (includeUncollected) {
            List<OshScanStatusDto> uncollectedScans = getUncollectedScans(page, size);
            results.addAll(uncollectedScans);
        }

        return results;
    }

    private List<OshScanStatusDto> getCollectedScans(int page, int size) {
        List<Job> jobsWithOshScanId = jobRepository.findJobsWithOshScanId(page, size);
        return jobsWithOshScanId.stream().map(this::convertJobToScanStatusDto).toList();
    }

    private List<OshScanStatusDto> getUncollectedScans(int page, int size) {
        List<OshUncollectedScan> uncollectedScans = oshUncollectedScanRepository.findAllWithPagination(page, size);
        return uncollectedScans.stream().map(this::convertUncollectedScanToDto).toList();
    }

    /**
     * Converts a Job entity with oshScanId to OshScanStatusDto.
     */
    private OshScanStatusDto convertJobToScanStatusDto(Job job) {
        OshScanStatusDto dto = new OshScanStatusDto();
        dto.setOshScanId(job.getOshScanId());
        dto.setPackageName(job.getPackageName());
        dto.setPackageNvr(job.getPackageNvr());
        dto.setStatus(STATUS_COLLECTED);
        dto.setAssociatedJob(JobMapper.INSTANCE.jobToJobResponseDto(job));
        dto.setRetryInfo(null);
        dto.setProcessedAt(job.getCreatedAt());
        dto.setOshTaskUrl(buildOshTaskUrl(job.getOshScanId()));
        return dto;
    }

    /**
     * Converts an OshUncollectedScan entity to OshScanStatusDto.
     */
    private OshScanStatusDto convertUncollectedScanToDto(OshUncollectedScan uncollectedScan) {
        OshScanStatusDto dto = new OshScanStatusDto();
        String oshScanId = String.valueOf(uncollectedScan.getOshScanId());
        dto.setOshScanId(oshScanId);
        dto.setPackageName(uncollectedScan.getPackageName());
        dto.setPackageNvr(uncollectedScan.getPackageNvr());
        dto.setStatus(STATUS_UNCOLLECTED);
        dto.setAssociatedJob(null);

        OshScanStatusDto.OshRetryInfoDto retryInfo = new OshScanStatusDto.OshRetryInfoDto();
        retryInfo.setRetryAttempts(uncollectedScan.getAttemptCount());
        retryInfo.setMaxRetries(oshConfiguration.hasRetryLimit() ? oshConfiguration.getRetryMaxAttempts() : -1);
        retryInfo.setFailureReason(uncollectedScan.getFailureReason().name());
        retryInfo.setLastAttemptAt(uncollectedScan.getLastAttemptAt());
        dto.setRetryInfo(retryInfo);

        dto.setProcessedAt(uncollectedScan.getCreatedAt());
        dto.setOshTaskUrl(buildOshTaskUrl(oshScanId));
        return dto;
    }

    /**
     * Gets job activity statistics for a specified time period.
     * Returns data points showing job counts by status at appropriate intervals.
     *
     * @param timePeriod the time period configuration (1h, 6h, 12h, 24h, 7d, 30d)
     * @return list of data points with job counts
     */
    public List<JobActivityDataPointDto> getJobActivity(TimePeriod timePeriod) {
        List<JobActivityDataPointDto> dataPoints = new ArrayList<>();
        Instant now = Instant.now();

        int numPoints = timePeriod.getDataPoints();
        long intervalSeconds = timePeriod.getIntervalSeconds();

        for (int i = numPoints - 1; i >= 0; i--) {
            Instant intervalEnd = now.minusSeconds(i * intervalSeconds);
            Instant intervalStart = intervalEnd.minusSeconds(intervalSeconds);

            JobActivityDataPointDto dataPoint = new JobActivityDataPointDto();
            dataPoint.setTimestamp(intervalStart.toString());
            dataPoint.setRunning(
                    jobRepository.countByStatusInTimeWindow(JobStatus.RUNNING, intervalStart, intervalEnd));
            dataPoint.setPending(
                    jobRepository.countByStatusInTimeWindow(JobStatus.PENDING, intervalStart, intervalEnd));
            dataPoint.setCompleted(
                    jobRepository.countByStatusInTimeWindow(JobStatus.COMPLETED, intervalStart, intervalEnd));
            dataPoint.setFailed(jobRepository.countByStatusInTimeWindow(JobStatus.FAILED, intervalStart, intervalEnd));

            dataPoints.add(dataPoint);
        }

        return dataPoints;
    }

    /**
     * Builds the OSH task URL for a given scan ID.
     *
     * @param oshScanId the OSH scan ID
     * @return the full URL to the OSH task page
     */
    private String buildOshTaskUrl(String oshScanId) {
        return String.format("%s/osh/task/%s", oshConfiguration.getBaseUrl(), oshScanId);
    }
}
