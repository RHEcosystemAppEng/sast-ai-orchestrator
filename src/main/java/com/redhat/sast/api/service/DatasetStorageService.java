package com.redhat.sast.api.service;

import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.redhat.sast.api.platform.storage.FilesystemStorageProvider;
import com.redhat.sast.api.platform.storage.MinioStorageProvider;
import com.redhat.sast.api.platform.storage.StorageProvider;
import com.redhat.sast.api.v1.dto.response.DatasetStorageHealthResponseDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing dataset storage operations and health checks.
 * Provides high-level operations for dataset storage management.
 * Supports filesystem and S3-compatible (MinIO) storage.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class DatasetStorageService {

    private final StorageProvider storageProvider;

    @Inject
    public DatasetStorageService(
            @ConfigProperty(name = "sast.ai.storage.type", defaultValue = "filesystem") String storageType,
            MinioStorageProvider minioStorageProvider,
            FilesystemStorageProvider filesystemStorageProvider) {
        this.storageProvider = switch (storageType.toLowerCase()) {
            case "s3", "minio" -> {
                LOGGER.debug("Using MinIO S3 storage provider");
                yield minioStorageProvider;
            }
            case "filesystem" -> {
                LOGGER.debug("Using filesystem storage provider");
                yield filesystemStorageProvider;
            }
            default -> {
                LOGGER.warn("Unknown storage type '{}', falling back to filesystem", storageType);
                yield filesystemStorageProvider;
            }
        };

        LOGGER.info("Dataset storage service initialized with: {}", storageProvider.getStorageInfo());
    }

    /**
     * Initializes dataset storage if enabled and not already present.
     * Supports S3-compatible storage initialization.
     *
     * @return true if storage is ready (either already existed or was created)
     */
    public boolean initializeDatasetStorage() {
        LOGGER.debug("Initializing dataset storage...");

        if ("s3".equals(storageProvider.getStorageType())) {
            try {
                storageProvider
                        .createBucketIfNotExists("sast-datasets")
                        .thenCompose(v -> storageProvider.createBucketIfNotExists("sast-temp"))
                        .toCompletableFuture()
                        .join();
                LOGGER.debug("S3 dataset storage initialized successfully");
                return true;
            } catch (Exception e) {
                LOGGER.warn("Failed to initialize S3 dataset storage", e);
                return false;
            }
        }

        LOGGER.debug("Filesystem storage initialization completed");
        return true;
    }

    /**
     * Checks the health status of dataset storage.
     * Supports filesystem and S3-compatible storage.
     *
     * @return DatasetStorageHealthResponseDto with status information
     */
    public DatasetStorageHealthResponseDto checkStorageHealth() {
        try {
            boolean isHealthy =
                    storageProvider.isHealthy().toCompletableFuture().join();
            return new DatasetStorageHealthResponseDto(isHealthy);
        } catch (Exception e) {
            LOGGER.warn("Storage health check failed", e);
            return new DatasetStorageHealthResponseDto(false);
        }
    }

    /**
     * Get the storage provider type.
     *
     * @return the storage type ("filesystem", "s3", etc.)
     */
    public String getStorageType() {
        return storageProvider.getStorageType();
    }

    /**
     * Get storage provider information.
     *
     * @return storage information string
     */
    public String getStorageInfo() {
        return storageProvider.getStorageInfo();
    }

    /**
     * Create a bucket/directory if it doesn't exist (async).
     *
     * @param bucketName the name of the bucket to create
     * @return CompletionStage that completes when bucket is created
     */
    public CompletionStage<Void> createBucketIfNotExists(String bucketName) {
        return storageProvider.createBucketIfNotExists(bucketName);
    }

    /**
     * Get the underlying storage provider for advanced operations.
     *
     * @return the storage provider instance
     */
    public StorageProvider getStorageProvider() {
        return storageProvider;
    }
}
