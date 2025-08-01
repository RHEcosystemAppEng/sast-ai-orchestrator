package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageSummaryDto {

    @JsonProperty("packageName")
    private String packageName;

    @JsonProperty("totalAnalyses")
    private Integer totalAnalyses;

    @JsonProperty("lastAnalysisDate")
    private LocalDateTime lastAnalysisDate;

    @JsonProperty("completedAnalyses")
    private Integer completedAnalyses;

    @JsonProperty("failedAnalyses")
    private Integer failedAnalyses;

    @JsonProperty("runningAnalyses")
    private Integer runningAnalyses;
}
