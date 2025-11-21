package com.redhat.sast.api.v1.dto.response;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class HealthResponseDto {

    @JsonProperty("status")
    private String status;

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("version")
    private String version;

    @JsonProperty("dependencies")
    private Map<String, String> dependencies;

    @JsonProperty("component")
    private String component;

    @JsonProperty("message")
    private String message;

    public HealthResponseDto() {
        this.timestamp = Instant.now();
        this.dependencies = new HashMap<>();
    }

    private HealthResponseDto(String status, String component, String message) {
        this();
        this.status = status;
        this.component = component;
        this.message = message;
    }

    // Static factory methods for component health checks
    public static HealthResponseDto up(String component) {
        return new HealthResponseDto("UP", component, null);
    }

    public static HealthResponseDto up(String component, String message) {
        return new HealthResponseDto("UP", component, message);
    }

    public static HealthResponseDto down(String component, String message) {
        return new HealthResponseDto("DOWN", component, message);
    }

    // Static factory method for overall application health
    public static HealthResponseDto overall() {
        return new HealthResponseDto();
    }

    // Automatically determine overall health based on dependencies
    public void determineOverallHealth() {
        if (dependencies == null || dependencies.isEmpty()) {
            this.status = "UP"; // No dependencies means healthy
            return;
        }

        // Check if any dependency is DOWN
        boolean hasDownDependency =
                dependencies.values().stream().anyMatch(status -> status != null && status.startsWith("DOWN"));

        this.status = hasDownDependency ? "DOWN" : "UP";
    }

    // Convenience method that determines health and returns this instance (for method chaining)
    public HealthResponseDto withOverallHealth() {
        determineOverallHealth();
        return this;
    }

    // Custom getter/setter for dependencies with defensive copying
    public Map<String, String> getDependencies() {
        return dependencies != null ? new HashMap<>(dependencies) : new HashMap<>();
    }

    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies != null ? new HashMap<>(dependencies) : new HashMap<>();
    }

    // Helper method to add a component health check result to dependencies
    public void addDependency(String componentName, String status) {
        if (this.dependencies == null) {
            this.dependencies = new HashMap<>();
        }
        this.dependencies.put(componentName, status);
    }

    // Helper method to add a component health check result from another HealthResponseDto
    public void addDependency(HealthResponseDto componentHealth) {
        if (componentHealth.getComponent() != null) {
            String dependencyStatus = componentHealth.getStatus();
            if (componentHealth.getMessage() != null) {
                dependencyStatus += " - " + componentHealth.getMessage();
            }
            addDependency(componentHealth.getComponent(), dependencyStatus);
        }
    }
}
