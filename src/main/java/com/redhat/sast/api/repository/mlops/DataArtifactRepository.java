package com.redhat.sast.api.repository.mlops;

import java.time.Instant;
import java.util.List;

import com.redhat.sast.api.model.DataArtifact;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for managing DataArtifact entities.
 * Provides data access methods for DVC artifact tracking.
 */
@ApplicationScoped
public class DataArtifactRepository implements PanacheRepository<DataArtifact> {

    /**
     * Finds artifacts by DVC version
     */
    public List<DataArtifact> findByVersion(String version) {
        return find("version = ?1 order by createdAt desc", version).list();
    }

    /**
     * Finds artifacts by type
     */
    public List<DataArtifact> findByType(String artifactType) {
        return find("artifactType = ?1 order by createdAt desc", artifactType).list();
    }

    /**
     * Finds artifacts by name pattern
     */
    public List<DataArtifact> findByNamePattern(String namePattern) {
        return find("name like ?1 order by createdAt desc", "%" + namePattern + "%")
                .list();
    }

    /**
     * Counts artifacts by type
     */
    public Long countByType(String artifactType) {
        return count("artifactType = ?1", artifactType);
    }

    /**
     * Gets latest artifacts for each type (requires native query for DISTINCT ON)
     */
    @SuppressWarnings("unchecked")
    public List<DataArtifact> findLatestByType() {
        return getEntityManager()
                .createNativeQuery(
                        """
                SELECT * FROM data_artifacts WHERE artifact_id IN (
                    SELECT DISTINCT ON (artifact_type) artifact_id
                    FROM data_artifacts
                    ORDER BY artifact_type, created_at DESC
                )
                ORDER BY artifact_type
                """,
                        DataArtifact.class)
                .getResultList();
    }

    /**
     * Deletes old artifacts (cleanup utility)
     */
    public long deleteOlderThan(Instant cutoffDate) {
        return delete("createdAt < ?1", cutoffDate);
    }
}
