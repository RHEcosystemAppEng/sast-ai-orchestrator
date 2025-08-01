package com.redhat.sast.api.service;

import java.util.Map;

import org.jboss.logging.Logger;

import com.redhat.sast.api.exceptions.InvalidNvrException;
import com.redhat.sast.api.util.url.NvrParser;
import com.redhat.sast.api.util.url.UrlInferenceOperation;
import com.redhat.sast.api.util.url.UrlInferenceOperations.KnownFalsePositivesUrlOperation;
import com.redhat.sast.api.util.url.UrlInferenceOperations.PackageNameOperation;
import com.redhat.sast.api.util.url.UrlInferenceOperations.ProjectNameOperation;
import com.redhat.sast.api.util.url.UrlInferenceOperations.ProjectVersionOperation;
import com.redhat.sast.api.util.url.UrlInferenceOperations.SourceCodeUrlOperation;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Service for resolving package NVR (Name-Version-Release) strings into various components
 * and derived information. This service can extract package components, infer URLs for
 * various resources like known false positives and source code, and validate NVR formats
 * using the Template Method pattern.
 */
@ApplicationScoped
public class NvrResolutionService {

    private static final Logger LOGGER = Logger.getLogger(NvrResolutionService.class);

    @Inject
    NvrParser nvrParser;

    private Map<String, UrlInferenceOperation> operations;

    public void setNvrParser(NvrParser nvrParser) {
        this.nvrParser = nvrParser;
        initOperations(); // Reinitialize operations when parser changes
    }

    @PostConstruct
    void initOperations() {
        operations = Map.of(
                "knownFalsePositives", new KnownFalsePositivesUrlOperation(nvrParser, LOGGER),
                "sourceCode", new SourceCodeUrlOperation(nvrParser, LOGGER),
                "projectName", new ProjectNameOperation(nvrParser, LOGGER),
                "projectVersion", new ProjectVersionOperation(nvrParser, LOGGER),
                "packageName", new PackageNameOperation(nvrParser, LOGGER));
    }

    /**
     * Resolves the known false positives URL from a package NVR.
     *
     * Pattern: https://gitlab.cee.redhat.com/osh/known-false-positives/-/raw/master/{packageName}/ignore.err
     *
     * @param packageNvr the package NVR (e.g., "systemd-257-9.el10")
     * @return the resolved URL
     * @throws InvalidNvrException if NVR is invalid or parsing fails
     */
    public String resolveKnownFalsePositivesUrl(String packageNvr) {
        return operations.get("knownFalsePositives").execute(packageNvr);
    }

    /**
     * Resolves the source code URL from a package NVR.
     *
     * Pattern: https://download.devel.redhat.com/brewroot/vol/rhel-{rhelVersion}/packages/{packageName}/{version}/{release}/src/{fullNvr}.src.rpm
     *
     * @param packageNvr the package NVR (e.g., "systemd-257-9.el10")
     * @return the resolved source code URL
     * @throws InvalidNvrException if NVR is invalid or parsing fails
     */
    public String resolveSourceCodeUrl(String packageNvr) {
        return operations.get("sourceCode").execute(packageNvr);
    }

    /**
     * Resolves the project name from a package NVR.
     *
     * @param packageNvr the package NVR (e.g., "systemd-257-9.el10")
     * @return the resolved project name (e.g., "systemd")
     * @throws InvalidNvrException if NVR is invalid or parsing fails
     */
    public String resolveProjectName(String packageNvr) {
        return operations.get("projectName").execute(packageNvr);
    }

    /**
     * Resolves the project version from a package NVR.
     * The project version is the version + "-" + release (without the .el part).
     *
     * @param packageNvr the package NVR (e.g., "systemd-257-9.el10")
     * @return the resolved project version (e.g., "257-9")
     * @throws InvalidNvrException if NVR is invalid or parsing fails
     */
    public String resolveProjectVersion(String packageNvr) {
        return operations.get("projectVersion").execute(packageNvr);
    }

    /**
     * Resolves the package name from a package NVR.
     * Note: This is identical to resolveProjectName() as package name and project name are the same.
     *
     * @param packageNvr the package NVR (e.g., "systemd-257-9.el10")
     * @return the resolved package name (e.g., "systemd")
     * @throws InvalidNvrException if NVR is invalid or parsing fails
     */
    public String resolvePackageName(String packageNvr) {
        return operations.get("packageName").execute(packageNvr);
    }

    /**
     * Validates if a package NVR is valid and can be used for resolution operations.
     *
     * @param packageNvr the package NVR to validate
     * @return true if the NVR is valid for resolution, false otherwise
     */
    public boolean isValidNvr(String packageNvr) {
        return nvrParser.isValidNvr(packageNvr);
    }
}
