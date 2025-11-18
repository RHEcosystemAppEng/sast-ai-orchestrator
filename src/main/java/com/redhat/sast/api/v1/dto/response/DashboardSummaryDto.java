package com.redhat.sast.api.v1.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing aggregated dashboard statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    @JsonProperty("totalJobs")
    private Long totalJobs;

    @JsonProperty("pendingJobs")
    private Long pendingJobs;

    @JsonProperty("runningJobs")
    private Long runningJobs;

    @JsonProperty("completedJobs")
    private Long completedJobs;

    @JsonProperty("failedJobs")
    private Long failedJobs;

    @JsonProperty("cancelledJobs")
    private Long cancelledJobs;

    @JsonProperty("totalBatches")
    private Long totalBatches;

    @JsonProperty("processingBatches")
    private Long processingBatches;

    @JsonProperty("completedBatches")
    private Long completedBatches;

    @JsonProperty("totalOshScans")
    private Long totalOshScans;

    @JsonProperty("collectedOshScans")
    private Long collectedOshScans;

    @JsonProperty("uncollectedOshScans")
    private Long uncollectedOshScans;

    @JsonProperty("timestamp")
    private Instant timestamp;
}
