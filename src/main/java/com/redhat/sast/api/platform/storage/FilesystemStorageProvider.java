package com.redhat.sast.api.platform.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Filesystem-based storage provider implementation.
 * Used as fallback when MinIO is not configured.
 */
@ApplicationScoped
public class FilesystemStorageProvider implements StorageProvider {

    private static final Logger LOG = Logger.getLogger(FilesystemStorageProvider.class);

    private final String baseStoragePath;

    @Inject
    public FilesystemStorageProvider(
            @ConfigProperty(name = "sast.ai.storage.filesystem.base-path", defaultValue = "/tmp/sast-ai-storage")
                    String baseStoragePath) {
        this.baseStoragePath = baseStoragePath;
    }

    @Override
    public CompletionStage<Boolean> isHealthy() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path basePath = Paths.get(baseStoragePath);
                if (!Files.exists(basePath)) {
                    Files.createDirectories(basePath);
                }
                return Files.isWritable(basePath);
            } catch (IOException e) {
                LOG.warn("Filesystem health check failed", e);
                return false;
            }
        });
    }

    @Override
    public CompletionStage<Boolean> bucketExists(String bucketName) {
        return CompletableFuture.supplyAsync(() -> {
            Path bucketPath = Paths.get(baseStoragePath, bucketName);
            return Files.exists(bucketPath) && Files.isDirectory(bucketPath);
        });
    }

    @Override
    public CompletionStage<Void> createBucketIfNotExists(String bucketName) {
        return bucketExists(bucketName).thenCompose(exists -> {
            if (Boolean.TRUE.equals(exists)) {
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.runAsync(() -> {
                try {
                    Path bucketPath = Paths.get(baseStoragePath, bucketName);
                    Files.createDirectories(bucketPath);
                    LOG.infof("Created filesystem directory: %s", bucketPath);
                } catch (IOException e) {
                    LOG.errorf("Failed to create filesystem directory for bucket: %s", bucketName, e);
                    throw new RuntimeException("Failed to create filesystem directory: " + bucketName, e);
                }
            });
        });
    }

    @Override
    public String getStorageType() {
        return "filesystem";
    }

    @Override
    public String getStorageInfo() {
        return String.format("Filesystem Storage - Base path: %s", baseStoragePath);
    }
}
