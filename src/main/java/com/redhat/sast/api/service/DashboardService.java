package com.redhat.sast.api.service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.repository.JobBatchRepository;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.repository.OshUncollectedScanRepository;
import com.redhat.sast.api.v1.dto.response.DashboardSummaryDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for generating dashboard summary statistics.
 */
@ApplicationScoped
@Slf4j
public class DashboardService {

    private static final long CACHE_TTL_SECONDS = 30; // Cache for 30 seconds

    @Inject
    JobRepository jobRepository;

    @Inject
    JobBatchRepository jobBatchRepository;

    @Inject
    OshUncollectedScanRepository oshUncollectedScanRepository;

    private final AtomicReference<CachedSummary> cachedSummary = new AtomicReference<>();

    /**
     * Gets dashboard summary statistics with caching.
     *
     * @return aggregated dashboard statistics
     */
    public DashboardSummaryDto getSummary() {
        CachedSummary cached = cachedSummary.get();

        if (cached != null && !cached.isExpired()) {
            LOGGER.debug("Returning cached dashboard summary (age: {} seconds)", cached.getAgeSeconds());
            return cached.summary;
        }

        DashboardSummaryDto summary = calculateSummary();

        cachedSummary.set(new CachedSummary(summary, LocalDateTime.now()));

        LOGGER.debug("Calculated and cached new dashboard summary");
        return summary;
    }

    /**
     * Invalidates the cache, forcing next request to recalculate.
     */
    public void invalidateCache() {
        cachedSummary.set(null);
        LOGGER.debug("Dashboard summary cache invalidated");
    }

    /**
     * Calculates dashboard summary by executing database queries.
     */
    private DashboardSummaryDto calculateSummary() {
        DashboardSummaryDto dto = new DashboardSummaryDto();

        // Job statistics
        dto.setTotalJobs(jobRepository.count());
        dto.setPendingJobs(jobRepository.count("status", JobStatus.PENDING));
        dto.setRunningJobs(jobRepository.count("status", JobStatus.RUNNING));
        dto.setCompletedJobs(jobRepository.count("status", JobStatus.COMPLETED));
        dto.setFailedJobs(jobRepository.count("status", JobStatus.FAILED));
        dto.setCancelledJobs(jobRepository.count("status", JobStatus.CANCELLED));

        // Batch statistics
        dto.setTotalBatches(jobBatchRepository.count());
        dto.setProcessingBatches(jobBatchRepository.count("status", BatchStatus.PROCESSING));
        dto.setCompletedBatches(jobBatchRepository.count("status", BatchStatus.COMPLETED));

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
     * Internal class for cache entry with expiration.
     */
    private static class CachedSummary {
        private final DashboardSummaryDto summary;
        private final LocalDateTime cachedAt;

        public CachedSummary(DashboardSummaryDto summary, LocalDateTime cachedAt) {
            this.summary = summary;
            this.cachedAt = cachedAt;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(cachedAt.plusSeconds(CACHE_TTL_SECONDS));
        }

        public long getAgeSeconds() {
            return java.time.Duration.between(cachedAt, LocalDateTime.now()).getSeconds();
        }
    }
}
