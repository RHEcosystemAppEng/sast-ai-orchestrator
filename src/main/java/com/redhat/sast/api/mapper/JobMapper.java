package com.redhat.sast.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;

@Mapper
public interface JobMapper {

    JobMapper INSTANCE = Mappers.getMapper(JobMapper.class);

    @Mapping(source = "id", target = "jobId")
    @Mapping(source = "packageSourceCodeUrl", target = "sourceCodeUrl")
    @Mapping(source = "jobBatch.id", target = "batchId")
    @Mapping(target = "cachedResult", ignore = true)
    @Mapping(target = "existingRun", ignore = true)
    @Mapping(target = "originalScanDate", ignore = true)
    @Mapping(target = "resultGoogleSheetUrl", ignore = true)
    JobResponseDto jobToJobResponseDto(Job job);

    /**
     * Converts a Job to JobResponseDto with cache status indicators.
     * Used when returning cached or existing running job results.
     *
     * @param job the job entity
     * @param isCached true if this is a cached result from a previously completed scan
     * @param isExistingRun true if there's already a running job for this NVR
     * @return the response DTO with appropriate flags set
     */
    default JobResponseDto jobToJobResponseDto(Job job, boolean isCached, boolean isExistingRun) {
        JobResponseDto dto = jobToJobResponseDto(job);
        dto.setCachedResult(isCached);
        dto.setExistingRun(isExistingRun);

        if (isCached && job.getCompletedAt() != null) {
            dto.setOriginalScanDate(job.getCompletedAt());
            // Include Google Sheet URL for cached results
            dto.setResultGoogleSheetUrl(job.getGSheetUrl());
        }

        return dto;
    }
}
