package com.redhat.sast.api.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for resolving package lists for MLOps batch processing.
 * Currently returns a hardcoded list for testing purposes.
 * In the future, this will use DVC to fetch package lists based on the provided NVR version.
 */
@ApplicationScoped
@Slf4j
public class PackageListResolver {

    /**
     * Retrieves a list of package NVRs for the given version.
     * 
     * @param packageNvr The package NVR or version identifier (currently unused, for future DVC integration)
     * @return List of package NVRs to process
     */
    public List<String> getPackageListForNvr(String packageNvr) {
        LOGGER.info("Resolving package list for NVR: {} (using hardcoded test list)", packageNvr);
        
        // Hardcoded list for testing
        // TODO: Fetch from DVC repository based on packageNvr
        List<String> packages = List.of(
            "libconfig-1.7.3-8.el10",
            "tpm2-tools-5.6-2.el10"
        );
        
        LOGGER.debug("Resolved {} packages for NVR: {}", packages.size(), packageNvr);
        return packages;
    }
}

