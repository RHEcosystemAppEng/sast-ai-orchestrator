package com.redhat.sast.api.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.redhat.sast.api.config.OshConfiguration;
import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.mapper.JobMapper;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.repository.JobRepository.OshScanStats;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;
import com.redhat.sast.api.v1.dto.response.MonitoredPackageWithScansDto;
import com.redhat.sast.api.v1.dto.response.PackageSummaryDto;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class PackageService {

    private final JobRepository jobRepository;
    private final OshConfiguration oshConfiguration;

    public List<PackageSummaryDto> getAllPackages(int page, int size) {
        List<String> packageNames = jobRepository.findDistinctPackageNames();

        return packageNames.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::getPackageSummary)
                .collect(Collectors.toList());
    }

    public PackageSummaryDto getPackageSummary(String packageName) {
        List<Job> jobs = jobRepository.findByPackageName(packageName);

        PackageSummaryDto summary = new PackageSummaryDto();
        summary.setPackageName(packageName);
        summary.setTotalAnalyses(jobs.size());

        // Calculate statistics
        long completedCount =
                jobs.stream().filter(j -> j.getStatus() == JobStatus.COMPLETED).count();
        long failedCount =
                jobs.stream().filter(j -> j.getStatus() == JobStatus.FAILED).count();
        long runningCount = jobs.stream()
                .filter(j -> j.getStatus() == JobStatus.RUNNING || j.getStatus() == JobStatus.SCHEDULED)
                .count();

        summary.setCompletedAnalyses((int) completedCount);
        summary.setFailedAnalyses((int) failedCount);
        summary.setRunningAnalyses((int) runningCount);

        // Find last analysis date
        Instant lastAnalysis =
                jobs.stream().map(Job::getCreatedAt).max(Instant::compareTo).orElse(null);
        summary.setLastAnalysisDate(lastAnalysis);

        return summary;
    }

    public List<JobResponseDto> getPackageJobs(String packageName, int page, int size) {
        return jobRepository.findJobsWithPagination(packageName, null, Page.of(page, size)).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets all monitored packages with their OSH scan statistics.
     * Combines the list of monitored packages from OshConfiguration with scan counts from the Job repository.
     *
     * @return list of monitored packages with OSH scan statistics
     */
    public List<MonitoredPackageWithScansDto> getMonitoredPackagesWithScans() {
        Map<String, OshScanStats> scanStatsMap = jobRepository.getOshScanStatsByPackage();

        return oshConfiguration.getPackageNameSet().stream()
                .map(packageName -> {
                    OshScanStats stats = scanStatsMap.get(packageName);

                    // If package has no OSH scans, use zeros
                    if (stats == null) {
                        return new MonitoredPackageWithScansDto(packageName, 0, null, 0, 0);
                    }

                    return new MonitoredPackageWithScansDto(
                            packageName,
                            stats.getScanCount(),
                            stats.getLastScanDate(),
                            stats.getCompletedScans(),
                            stats.getFailedScans());
                })
                .collect(Collectors.toList());
    }

    private JobResponseDto convertToResponseDto(Job job) {
        return JobMapper.INSTANCE.jobToJobResponseDto(job);
    }
}
