package com.redhat.sast.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.repository.JobRepository;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;
import com.redhat.sast.api.v1.dto.response.PackageSummaryDto;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PackageService {

    @Inject
    JobRepository jobRepository;

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
        LocalDateTime lastAnalysis = jobs.stream()
                .map(Job::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        summary.setLastAnalysisDate(lastAnalysis);

        return summary;
    }

    public List<JobResponseDto> getPackageJobs(String packageName, int page, int size) {
        return jobRepository.findJobsWithPagination(packageName, null, Page.of(page, size)).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
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
}
