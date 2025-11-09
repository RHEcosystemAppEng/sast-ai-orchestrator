package com.redhat.sast.api.service.osh;

import java.util.ArrayList;
import java.util.List;

import com.redhat.sast.api.config.OshConfiguration;
import com.redhat.sast.api.mapper.JobMapper;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.OshUncollectedScan;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.repository.OshUncollectedScanRepository;
import com.redhat.sast.api.v1.dto.response.OshRetryInfoDto;
import com.redhat.sast.api.v1.dto.response.OshScanStatusDto;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for querying OSH scan information for dashboard and monitoring.
 *
 * Provides aggregated views combining:
 * - Collected scans (jobs with oshScanId)
 * - Uncollected scans (retry queue)
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class OshQueryService {

    private static final String STATUS_COLLECTED = "COLLECTED";
    private static final String STATUS_UNCOLLECTED = "UNCOLLECTED";
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final JobRepository jobRepository;
    private final OshUncollectedScanRepository uncollectedScanRepository;
    private final OshConfiguration oshConfiguration;

    /**
     * Retrieves all OSH scans (both collected and uncollected) with pagination and filtering.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @param status filter by status: "COLLECTED", "UNCOLLECTED", or null for all
     * @return list of OSH scans with associated job and retry information
     */
    public List<OshScanStatusDto> getAllScans(Integer page, Integer size, String status) {
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

    /**
     * Gets collected OSH scans (jobs with oshScanId) with database-level pagination.
     */
    private List<OshScanStatusDto> getCollectedScans(int page, int size) {
        List<Job> jobsWithOshScanId = jobRepository
                .getEntityManager()
                .createQuery("SELECT j FROM Job j WHERE j.oshScanId IS NOT NULL ORDER BY j.createdAt DESC", Job.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        return jobsWithOshScanId.stream().map(this::convertJobToScanDto).toList();
    }

    /**
     * Gets uncollected OSH scans (retry queue) with database-level pagination.
     */
    private List<OshScanStatusDto> getUncollectedScans(int page, int size) {
        List<OshUncollectedScan> uncollectedScans = uncollectedScanRepository
                .getEntityManager()
                .createQuery("SELECT u FROM OshUncollectedScan u ORDER BY u.createdAt DESC", OshUncollectedScan.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        return uncollectedScans.stream().map(this::convertUncollectedScanToDto).toList();
    }

    /**
     * Converts a Job entity with oshScanId to OshScanStatusDto.
     */
    private OshScanStatusDto convertJobToScanDto(Job job) {
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
