package com.redhat.sast.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.redhat.sast.api.config.OshConfiguration;
import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.mapper.JobMapper;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.OshUncollectedScan;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.repository.OshUncollectedScanRepository;
import com.redhat.sast.api.v1.dto.response.DashboardSummaryDto;
import com.redhat.sast.api.v1.dto.response.OshRetryInfoDto;
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
     * Gets dashboard summary statistics.
     *
     * @return aggregated dashboard statistics
     */
    public DashboardSummaryDto getSummary() {
        return calculateSummary();
    }

    private DashboardSummaryDto calculateSummary() {
        DashboardSummaryDto dto = new DashboardSummaryDto();

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
        long collectedOshScans = countJobsWithOshScanId();
        long uncollectedOshScans = oshUncollectedScanRepository.count();

        dto.setCollectedOshScans(collectedOshScans);
        dto.setUncollectedOshScans(uncollectedOshScans);
        dto.setTotalOshScans(collectedOshScans + uncollectedOshScans);

        dto.setTimestamp(LocalDateTime.now());

        return dto;
    }

    /**
     * Counts jobs that have an associated OSH scan ID (collected scans).
     */
    private long countJobsWithOshScanId() {
        return jobRepository
                .getEntityManager()
                .createQuery("SELECT COUNT(j) FROM Job j WHERE j.oshScanId IS NOT NULL", Long.class)
                .getSingleResult();
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
        return dto;
    }

    /**
     * Converts an OshUncollectedScan entity to OshScanStatusDto.
     */
    private OshScanStatusDto convertUncollectedScanToDto(OshUncollectedScan uncollectedScan) {
        OshScanStatusDto dto = new OshScanStatusDto();
        dto.setOshScanId(String.valueOf(uncollectedScan.getOshScanId()));
        dto.setPackageName(uncollectedScan.getPackageName());
        dto.setPackageNvr(uncollectedScan.getPackageNvr());
        dto.setStatus(STATUS_UNCOLLECTED);
        dto.setAssociatedJob(null);

        OshRetryInfoDto retryInfo = new OshRetryInfoDto();
        retryInfo.setRetryAttempts(uncollectedScan.getAttemptCount());
        retryInfo.setMaxRetries(oshConfiguration.hasRetryLimit() ? oshConfiguration.getRetryMaxAttempts() : -1);
        retryInfo.setFailureReason(uncollectedScan.getFailureReason().name());
        retryInfo.setLastAttemptAt(uncollectedScan.getLastAttemptAt());
        dto.setRetryInfo(retryInfo);

        dto.setProcessedAt(uncollectedScan.getCreatedAt());
        return dto;
    }
}
