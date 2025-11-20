package com.redhat.sast.api.v1.dto.osh;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an OSH (Open Scan Hub) scan response.
 *
 * OSH API endpoint: GET /osh/task/{scanId}
 *
 * OSH can return both JSON and HTML responses for the same data.
 * This DTO structure accommodates both formats.
 */
@Data
@NoArgsConstructor
public class OshScanDto {

    /**
     * OSH scan ID - sequential integer (e.g., 1001, 1002, 1003...)
     * This is the primary identifier for OSH scans.
     */
    private Integer scanId;

    /**
     * Package/component name extracted from OSH label field.
     * Examples: "systemd", "kernel", "glibc"
     */
    private String component;

    /**
     * Package version extracted from OSH label field.
     * May be null if version cannot be parsed from label.
     */
    private String version;

    /**
     * OSH scan state - the primary field we filter on.
     * Valid values: "CLOSED", "OPEN", "FAILED"
     * We only process "CLOSED" scans.
     */
    private String state;

    /**
     * Owner/submitter of the OSH scan.
     * Used for filtering and attribution.
     */
    private String owner;

    /**
     * Scan type/method used by OSH.
     * Examples: "VersionDiffBuild", "MockBuild", etc.
     */
    private String scanType;

    /**
     * Scan creation timestamp as string (OSH format).
     * Raw format from OSH - parsing handled separately if needed.
     */
    private String created;

    /**
     * Scan start timestamp as string (OSH format).
     * May be null if scan hasn't started yet.
     */
    private String started;

    /**
     * Scan completion timestamp as string (OSH format).
     * Only present for completed scans.
     */
    private String finished;

    /**
     * Target architecture for the scan.
     * Examples: "x86_64", "aarch64"
     */
    private String arch;

    /**
     * OSH channel/stream information.
     */
    private String channel;

    /**
     * Raw OSH response data for debugging and future extensibility.
     * Preserves all original fields from OSH API response.
     */
    private Map<String, Object> rawData;

    /**
     * Constructs OshScanResponse with minimal required fields.
     *
     * @param scanId the OSH scan ID
     * @param state the scan state (CLOSED, OPEN, FAILED)
     */
    public OshScanDto(Integer scanId, String state) {
        this.scanId = scanId;
        this.state = state;
    }

    /**
     * Checks if this scan is completed and ready for processing.
     *
     * @return true if scan state is "CLOSED"
     */
    public boolean isCompleted() {
        return "CLOSED".equals(state);
    }

    /**
     * Extracts package name for filtering.
     * Falls back to component if version parsing failed.
     *
     * @return the package name for comparison
     */
    public String getPackageName() {
        return component;
    }
}
