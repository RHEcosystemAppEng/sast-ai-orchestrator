package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetStorageHealthResponseDto {

    @JsonProperty("ready")
    private boolean ready;

    @JsonProperty("status")
    private String status;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public DatasetStorageHealthResponseDto(boolean ready) {
        this.ready = ready;
        this.status = ready ? "READY" : "NOT_READY";
        this.timestamp = LocalDateTime.now();
    }
}
