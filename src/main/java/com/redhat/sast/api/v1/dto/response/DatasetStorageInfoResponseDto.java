package com.redhat.sast.api.v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for dataset storage information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasetStorageInfoResponseDto {

    /**
     * Type of storage provider (filesystem, s3, etc.).
     */
    private String storageType;

    /**
     * Detailed information about the storage provider.
     */
    private String storageInfo;
}
