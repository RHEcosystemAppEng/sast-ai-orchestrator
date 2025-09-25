package com.redhat.sast.api.platform.storage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * MinIO S3-compatible storage provider implementation.
 */
@ApplicationScoped
public class MinioStorageProvider implements StorageProvider {

    private static final Logger LOG = Logger.getLogger(MinioStorageProvider.class);

    private final MinioClient minioClient;
    private final ExecutorService executor;
    private final String datasetsBucket;
    private final String tempBucket;

    public MinioStorageProvider(
            @ConfigProperty(name = "sast.ai.storage.s3.endpoint") String endpoint,
            @ConfigProperty(name = "sast.ai.storage.s3.access-key") String accessKey,
            @ConfigProperty(name = "sast.ai.storage.s3.secret-key") String secretKey,
            @ConfigProperty(name = "sast.ai.storage.s3.use-ssl", defaultValue = "false") boolean useSsl,
            @ConfigProperty(name = "sast.ai.storage.s3.bucket.datasets") String datasetsBucket,
            @ConfigProperty(name = "sast.ai.storage.s3.bucket.temp") String tempBucket) {

        this.datasetsBucket = datasetsBucket;
        this.tempBucket = tempBucket;
        this.executor = Executors.newFixedThreadPool(4);

        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            LOG.infof("MinIO storage provider initialized - endpoint: %s, ssl: %s", endpoint, useSsl);
        } catch (Exception e) {
            LOG.error("Failed to initialize MinIO client", e);
            throw new RuntimeException("MinIO client initialization failed", e);
        }
    }

    void onStartup(@Observes StartupEvent ev) {
        LOG.info("MinIO storage provider starting up");

        // Initialize default buckets asynchronously
        createBucketIfNotExists(datasetsBucket)
                .thenCompose(v -> createBucketIfNotExists(tempBucket))
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        LOG.error("Failed to initialize MinIO buckets during startup", throwable);
                    } else {
                        LOG.info("MinIO storage provider startup completed successfully");
                    }
                });
    }

    @Override
    public CompletionStage<Boolean> isHealthy() {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        // Try to list buckets as a health check
                        minioClient.listBuckets();
                        return true;
                    } catch (Exception e) {
                        LOG.warn("MinIO health check failed", e);
                        return false;
                    }
                },
                executor);
    }

    @Override
    public CompletionStage<Boolean> bucketExists(String bucketName) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return minioClient.bucketExists(
                                BucketExistsArgs.builder().bucket(bucketName).build());
                    } catch (Exception e) {
                        LOG.warnf("Failed to check if bucket exists: %s", bucketName, e);
                        return false;
                    }
                },
                executor);
    }

    @Override
    public CompletionStage<Void> createBucketIfNotExists(String bucketName) {
        return bucketExists(bucketName).thenCompose(exists -> {
            if (exists) {
                LOG.debugf("Bucket already exists: %s", bucketName);
                return CompletableFuture.completedFuture(null);
            }

            return CompletableFuture.runAsync(
                    () -> {
                        try {
                            minioClient.makeBucket(
                                    MakeBucketArgs.builder().bucket(bucketName).build());
                            LOG.infof("Created bucket: %s", bucketName);
                        } catch (ErrorResponseException e) {
                            if ("BucketAlreadyOwnedByYou"
                                    .equals(e.errorResponse().code())) {
                                LOG.debugf("Bucket already exists (race condition): %s", bucketName);
                            } else {
                                LOG.errorf("Failed to create bucket: %s", bucketName, e);
                                throw new RuntimeException("Failed to create bucket: " + bucketName, e);
                            }
                        } catch (Exception e) {
                            LOG.errorf("Failed to create bucket: %s", bucketName, e);
                            throw new RuntimeException("Failed to create bucket: " + bucketName, e);
                        }
                    },
                    executor);
        });
    }

    @Override
    public String getStorageType() {
        return "s3";
    }

    @Override
    public String getStorageInfo() {
        return String.format("MinIO S3 Storage - Buckets: [datasets=%s, temp=%s]", datasetsBucket, tempBucket);
    }

    public String getDatasetsBucket() {
        return datasetsBucket;
    }

    public String getTempBucket() {
        return tempBucket;
    }

    public MinioClient getMinioClient() {
        return minioClient;
    }
}
