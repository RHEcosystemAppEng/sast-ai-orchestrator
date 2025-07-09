package com.redhat.sast.api.service;

import org.jboss.logging.Logger;

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

    // URL patterns for different resources
    private static final String KNOWN_FALSE_POSITIVES_BASE_URL =
            "https://gitlab.cee.redhat.com/osh/known-false-positives/-/raw/master/";
    private static final String KNOWN_FALSE_POSITIVES_FILE = "ignore.err";

    private static final String SOURCE_CODE_BASE_URL_TEMPLATE =
            "https://download.devel.redhat.com/brewroot/vol/rhel-%s/packages/";
    private static final String SOURCE_CODE_SUFFIX = ".src.rpm";

    @Inject
    NvrParser nvrParser;

    // Public setter for testing purposes
    public void setNvrParser(NvrParser nvrParser) {
        this.nvrParser = nvrParser;
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
        if (packageNvr == null || packageNvr.trim().isEmpty()) {
            LOG.warnf("Cannot infer known false positives URL: packageNvr is null or empty");
            return null;
        }

        String packageName = nvrParser.extractPackageName(packageNvr);
        if (packageName == null) {
            LOG.warnf(
                    "Cannot infer known false positives URL: failed to extract package name from NVR '%s'", packageNvr);
            return null;
        }

        String url = KNOWN_FALSE_POSITIVES_BASE_URL + packageName + "/" + KNOWN_FALSE_POSITIVES_FILE;
        LOG.infof("Inferred known false positives URL for NVR '%s': %s", packageNvr, url);
        return url;
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
        if (packageNvr == null || packageNvr.trim().isEmpty()) {
            LOG.warnf("Cannot infer source code URL: packageNvr is null or empty");
            return null;
        }

        String packageName = nvrParser.extractPackageName(packageNvr);
        String version = nvrParser.extractVersion(packageNvr);
        String release = nvrParser.extractRelease(packageNvr);
        String rhelVersion = nvrParser.extractRhelVersion(packageNvr);

        if (packageName == null || version == null || release == null || rhelVersion == null) {
            LOG.warnf("Cannot infer source code URL: failed to extract package components from NVR '%s'", packageNvr);
            return null;
        }

        String baseUrl = String.format(SOURCE_CODE_BASE_URL_TEMPLATE, rhelVersion);
        String url = baseUrl + packageName + "/" + version + "/" + release + "/src/" + packageNvr
                + SOURCE_CODE_SUFFIX;
        LOG.infof("Inferred source code URL for NVR '%s': %s", packageNvr, url);
        return url;
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
