package com.redhat.sast.api.service;

import java.time.LocalDateTime;

import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.repository.OshUncollectedScanRepository;
import com.redhat.sast.api.v1.dto.response.DashboardSummaryDto;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for generating dashboard summary statistics.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class StatisticService {

    private static final String STATUS_FIELD = "status";

    private final JobRepository jobRepository;
    private final JobBatchRepository jobBatchRepository;
    private final OshUncollectedScanRepository oshUncollectedScanRepository;

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
}
