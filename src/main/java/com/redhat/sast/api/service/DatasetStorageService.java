package com.redhat.sast.api.service;

import com.redhat.sast.api.platform.KubernetesResourceManager;
import com.redhat.sast.api.v1.dto.response.DatasetStorageHealthResponseDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing dataset storage operations and health checks.
 * Provides high-level operations for dataset storage management.
 */
@ApplicationScoped
@Slf4j
public class DatasetStorageService {

    @Inject
    KubernetesResourceManager kubernetesResourceManager;

    /**
     * Initializes dataset storage if enabled and not already present.
     *
     * @return true if storage is ready (either already existed or was created)
     */
    public boolean initializeDatasetStorage() {
        LOGGER.debug("Initializing dataset storage...");

        if (kubernetesResourceManager.isDatasetStorageReady()) {
            LOGGER.debug("Dataset storage is already ready");
            return true;
        }

        boolean created = kubernetesResourceManager.createDatasetStoragePVCs();
        if (created) {
            LOGGER.debug("Dataset storage initialized successfully");
            return true;
        } else {
            LOGGER.warn("Failed to initialize dataset storage");
            return false;
        }
    }

    /**
     * Checks the health status of dataset storage.
     *
     * @return DatasetStorageHealthResponseDto with status information
     */
    public DatasetStorageHealthResponseDto checkStorageHealth() {
        boolean isReady = kubernetesResourceManager.isDatasetStorageReady();
        return new DatasetStorageHealthResponseDto(isReady);
    }
}
