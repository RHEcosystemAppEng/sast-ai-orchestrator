package com.redhat.sast.api.v1.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.enums.BatchStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobBatchResponseDto {

    @JsonProperty("batchId")
    private Long batchId;

    @JsonProperty("batchGoogleSheetUrl")
    private String batchGoogleSheetUrl;

    @JsonProperty("submittedBy")
    private String submittedBy;

    @JsonProperty("submittedAt")
    private Instant submittedAt;

    @JsonProperty("status")
    private BatchStatus status;

    @JsonProperty("totalJobs")
    private Integer totalJobs;

    @JsonProperty("completedJobs")
    private Integer completedJobs;

    @JsonProperty("failedJobs")
    private Integer failedJobs;
}
