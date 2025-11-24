package com.redhat.sast.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.redhat.sast.api.model.MlOpsJobSettings;
import com.redhat.sast.api.v1.dto.request.JobSettingsDto;

@Mapper
public interface JobSettingsMapper {

    JobSettingsMapper INSTANCE = Mappers.getMapper(JobSettingsMapper.class);

    MlOpsJobSettings toEntity(JobSettingsDto dto);
}
