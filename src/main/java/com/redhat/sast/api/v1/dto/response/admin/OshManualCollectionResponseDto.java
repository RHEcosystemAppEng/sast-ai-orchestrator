package com.redhat.sast.api.v1.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.v1.dto.osh.OshScanDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for OSH manual collection endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OshManualCollectionResponseDto {

    @JsonProperty("scanData")
    private OshScanDto scanData;

    @JsonProperty("job")
    private Job job;

    @JsonProperty("jobCreated")
    private boolean jobCreated;

    /**
     * Detailed message explaining the result.
     */
    @JsonProperty("message")
    private String message;
}
