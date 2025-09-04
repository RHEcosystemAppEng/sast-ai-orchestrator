package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.enums.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDto {

    @JsonProperty("projectName")
    protected String projectName;

    @JsonProperty("projectVersion")
    protected String projectVersion;

    @JsonProperty("packageName")
    protected String packageName;

    @JsonProperty("packageNvr")
    protected String packageNvr;

    @JsonProperty("oshScanId")
    protected String oshScanId;

    @JsonProperty("jiraLink")
    protected String jiraLink;

    @JsonProperty("hostname")
    protected String hostname;

    @JsonProperty("status")
    protected JobStatus status;

    @JsonProperty("createdAt")
    protected LocalDateTime createdAt;

    @JsonProperty("startedAt")
    protected LocalDateTime startedAt;

    @JsonProperty("completedAt")
    protected LocalDateTime completedAt;

    @JsonProperty("cancelledAt")
    protected LocalDateTime cancelledAt;

    @JsonProperty("tektonUrl")
    protected String tektonUrl;

    @JsonProperty("jobId")
    private Long jobId;

    @JsonProperty("sourceCodeUrl")
    private String sourceCodeUrl;

    @JsonProperty("batchId")
    private Long batchId;

    @JsonProperty("submittedBy")
    private String submittedBy;
}
