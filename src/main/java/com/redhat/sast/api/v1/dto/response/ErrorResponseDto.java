package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    @JsonProperty("error")
    private String error;

    @JsonProperty("details")
    private String details;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public ErrorResponseDto(String error, String details) {
        this.error = error;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
