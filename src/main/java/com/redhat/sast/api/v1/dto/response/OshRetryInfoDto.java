package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing retry information for an OSH scan.
 *
 * Contains details about retry attempts, failure reasons, and timing.
 * Used as a nested object within OshScanWithJobDto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OshRetryInfoDto {

    @JsonProperty("retryAttempts")
    private Integer retryAttempts;

    @JsonProperty("maxRetries")
    private Integer maxRetries;

    @JsonProperty("failureReason")
    private String failureReason;

    @JsonProperty("lastAttemptAt")
    private LocalDateTime lastAttemptAt;
}
