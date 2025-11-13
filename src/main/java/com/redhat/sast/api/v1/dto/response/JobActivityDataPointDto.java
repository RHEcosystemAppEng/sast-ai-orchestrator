package com.redhat.sast.api.v1.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for job activity data point representing job counts at a specific time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobActivityDataPointDto {

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("running")
    private Long running;

    @JsonProperty("pending")
    private Long pending;

    @JsonProperty("completed")
    private Long completed;

    @JsonProperty("failed")
    private Long failed;
}
