package com.redhat.sast.api.v1.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredPackageWithScansDto {

    @JsonProperty("packageName")
    private String packageName;

    @JsonProperty("oshScanCount")
    private Integer oshScanCount;

    @JsonProperty("lastOshScanDate")
    private Instant lastOshScanDate;

    @JsonProperty("completedOshScans")
    private Integer completedOshScans;

    @JsonProperty("failedOshScans")
    private Integer failedOshScans;
}
