package com.redhat.sast.api.service;

import org.jboss.logging.Logger;

import com.redhat.sast.api.common.constants.UrlConstants;
import com.redhat.sast.api.util.url.NvrParser;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Service for inferring URLs based on package NVR (Name-Version-Release) strings.
 * This service constructs URLs for various resources like known false positives,
 * source code, etc., based on standardized URL patterns.
 */
@ApplicationScoped
public class UrlInferenceService {

    private static final Logger LOG = Logger.getLogger(UrlInferenceService.class);

    @Inject
    NvrParser nvrParser;

    // Public setter for testing purposes
    public void setNvrParser(NvrParser nvrParser) {
        this.nvrParser = nvrParser;
    }

    /**
     * Validates the packageNvr parameter.
     *
     * @param packageNvr the package NVR to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidNvr(String packageNvr) {
        return packageNvr != null && !packageNvr.trim().isEmpty();
    }

    /**
     * Infers the known false positives URL from a package NVR.
     *
     * Pattern: https://gitlab.cee.redhat.com/osh/known-false-positives/-/raw/master/{packageName}/ignore.err
     *
     * @param packageNvr the package NVR (e.g., "systemd-257-9.el10")
     * @return the inferred URL or null if NVR parsing fails
     */
    public String inferKnownFalsePositivesUrl(String packageNvr) {
        if (!isValidNvr(packageNvr)) {
            LOG.warnf("Cannot infer known false positives URL: packageNvr is null or empty");
            return null;
        }

        String packageName = nvrParser.extractPackageName(packageNvr);
        if (packageName == null) {
            LOG.warnf("Failed to extract package name from NVR '%s'", packageNvr);
            return null;
        }

        return UrlConstants.KNOWN_FALSE_POSITIVES_BASE_URL + packageName + "/"
                + UrlConstants.KNOWN_FALSE_POSITIVES_FILE;
    }

    /**
     * Infers the source code URL from a package NVR.
     *
     * Pattern: https://download.devel.redhat.com/brewroot/vol/rhel-{rhelVersion}/packages/{packageName}/{version}/{release}/src/{fullNvr}.src.rpm
     *
     * @param packageNvr the package NVR (e.g., "systemd-257-9.el10")
     * @return the inferred source code URL or null if NVR parsing fails
     */
    public String inferSourceCodeUrl(String packageNvr) {
        if (!isValidNvr(packageNvr)) {
            LOG.warnf("Cannot infer source code URL: packageNvr is null or empty");
            return null;
        }

        String packageName = nvrParser.extractPackageName(packageNvr);
        String version = nvrParser.extractVersion(packageNvr);
        String release = nvrParser.extractRelease(packageNvr);
        String rhelVersion = nvrParser.extractRhelVersion(packageNvr);

        if (packageName == null || version == null || release == null || rhelVersion == null) {
            LOG.warnf("Failed to extract package components from NVR '%s'", packageNvr);
            return null;
        }

        String baseUrl = String.format(UrlConstants.SOURCE_CODE_BASE_URL_TEMPLATE, rhelVersion);
        return baseUrl + packageName + "/" + version + "/" + release + "/src/" + packageNvr
                + UrlConstants.SOURCE_CODE_SUFFIX;
    }

    /**
     * Infers the project name from a package NVR.
     *
     * @param packageNvr the package NVR (e.g., "systemd-257-9.el10")
     * @return the inferred project name (e.g., "systemd") or null if NVR parsing fails
     */
    public String inferProjectName(String packageNvr) {
        if (!isValidNvr(packageNvr)) {
            LOG.warnf("Cannot infer project name: packageNvr is null or empty");
            return null;
        }

        String projectName = nvrParser.extractPackageName(packageNvr);
        if (projectName == null) {
            LOG.warnf("Failed to extract project name from NVR '%s'", packageNvr);
        }
        return projectName;
    }

    /**
     * Infers the project version from a package NVR.
     * The project version is the version + "-" + release (without the .el part).
     *
     * @param packageNvr the package NVR (e.g., "systemd-257-9.el10")
     * @return the inferred project version (e.g., "257-9") or null if NVR parsing fails
     */
    public String inferProjectVersion(String packageNvr) {
        if (!isValidNvr(packageNvr)) {
            LOG.warnf("Cannot infer project version: packageNvr is null or empty");
            return null;
        }

        String version = nvrParser.extractVersion(packageNvr);
        String release = nvrParser.extractRelease(packageNvr);

        if (version == null || release == null) {
            LOG.warnf("Failed to extract version/release from NVR '%s'", packageNvr);
            return null;
        }

        // Extract the release part before ".el" (e.g., "9.el10" -> "9")
        String releaseWithoutEl = release.split("\\.el")[0];
        return version + "-" + releaseWithoutEl;
    }

    /**
     * Infers the package name from a package NVR.
     * Note: This is identical to inferProjectName() as package name and project name are the same.
     *
     * @param packageNvr the package NVR (e.g., "systemd-257-9.el10")
     * @return the inferred package name (e.g., "systemd") or null if NVR parsing fails
     */
    public String inferPackageName(String packageNvr) {
        return inferProjectName(packageNvr);
    }

    /**
     * Validates if a package NVR can be used for URL inference.
     *
     * @param packageNvr the package NVR to validate
     * @return true if the NVR is valid for URL inference, false otherwise
     */
    public boolean canInferUrls(String packageNvr) {
        return nvrParser.isValidNvr(packageNvr);
    }
}
