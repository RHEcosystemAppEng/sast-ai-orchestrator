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
    JobResponseDto jobToJobResponseDto(Job job);
}
