package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthResponseDto {

    @JsonProperty("status")
    private String status;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("version")
    private String version;

    @JsonProperty("dependencies")
    private Map<String, String> dependencies;

    public HealthResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getDependencies() {
        return dependencies != null ? new HashMap<>(dependencies) : new HashMap<>();
    }

    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies != null ? new HashMap<>(dependencies) : new HashMap<>();
    }
}
