package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.enums.BatchStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlOpsBatchResponseDto {

    @JsonProperty("batchId")
    private Long batchId;

    @JsonProperty("testingDataNvrsVersion")
    private String testingDataNvrsVersion;

    @JsonProperty("promptsVersion")
    private String promptsVersion;

    @JsonProperty("knownNonIssuesVersion")
    private String knownNonIssuesVersion;

    @JsonProperty("sastAiImage")
    private String sastAiImage;

    @JsonProperty("submittedBy")
    private String submittedBy;

    @JsonProperty("submittedAt")
    private LocalDateTime submittedAt;

    @JsonProperty("status")
    private BatchStatus status;

    @JsonProperty("totalJobs")
    private Integer totalJobs;

    @JsonProperty("completedJobs")
    private Integer completedJobs;

    @JsonProperty("failedJobs")
    private Integer failedJobs;

    @JsonProperty("lastUpdatedAt")
    private LocalDateTime lastUpdatedAt;
}
