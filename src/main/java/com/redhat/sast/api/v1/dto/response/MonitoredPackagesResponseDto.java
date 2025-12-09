package com.redhat.sast.api.v1.dto.response;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing the list of packages currently being monitored in OSH (Open Scan Hub).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredPackagesResponseDto {

    @JsonProperty("packages")
    private Set<String> packages;

    @JsonProperty("oshEnabled")
    private boolean oshEnabled;

    @JsonProperty("totalPackages")
    private int totalPackages;

    @JsonProperty("packagesFilePath")
    private String packagesFilePath;
}
