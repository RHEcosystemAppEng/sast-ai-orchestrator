package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.enums.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlOpsJobResponseDto {

    @JsonProperty("jobId")
    private Long jobId;

    @JsonProperty("batchId")
    private Long batchId;

    @JsonProperty("packageNvr")
    private String packageNvr;

    @JsonProperty("packageName")
    private String packageName;

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("projectVersion")
    private String projectVersion;

    @JsonProperty("packageSourceCodeUrl")
    private String packageSourceCodeUrl;

    @JsonProperty("knownFalsePositivesUrl")
    private String knownFalsePositivesUrl;

    @JsonProperty("status")
    private JobStatus status;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("startedAt")
    private LocalDateTime startedAt;

    @JsonProperty("completedAt")
    private LocalDateTime completedAt;

    @JsonProperty("tektonUrl")
    private String tektonUrl;

    @JsonProperty("oshScanId")
    private String oshScanId;

    @JsonProperty("jiraLink")
    private String jiraLink;

    @JsonProperty("hostname")
    private String hostname;

    @JsonProperty("submittedBy")
    private String submittedBy;
}
