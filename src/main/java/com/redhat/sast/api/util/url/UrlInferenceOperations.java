package com.redhat.sast.api.util.url;

import org.slf4j.Logger;

import com.redhat.sast.api.common.constants.UrlConstants;

/**
 * Concrete implementations of URL inference operations.
 */
public final class UrlInferenceOperations {

    private UrlInferenceOperations() {
        // Utility class
    }

    /**
     * Operation for inferring known false positives URLs.
     */
    public static class KnownFalsePositivesUrlOperation extends UrlInferenceOperation {

        public KnownFalsePositivesUrlOperation(NvrParser nvrParser, Logger logger) {
            super(nvrParser, logger);
        }

        @Override
        protected String performInference(String packageNvr) {
            String packageName = nvrParser.extractPackageName(packageNvr);
            if (packageName == null) {
                throw new IllegalStateException("Could not extract package name from valid NVR: " + packageNvr);
            }

            return UrlConstants.KNOWN_FALSE_POSITIVES_BASE_URL + packageName + "/"
                    + UrlConstants.KNOWN_FALSE_POSITIVES_FILE;
        }

        @Override
        protected String getOperationName() {
            return "known false positives URL";
        }
    }

    /**
     * Operation for inferring source code URLs.
     */
    public static class SourceCodeUrlOperation extends UrlInferenceOperation {

        public SourceCodeUrlOperation(NvrParser nvrParser, Logger logger) {
            super(nvrParser, logger);
        }

        @Override
        protected String performInference(String packageNvr) {
            String packageName = nvrParser.extractPackageName(packageNvr);
            String version = nvrParser.extractVersion(packageNvr);
            String release = nvrParser.extractRelease(packageNvr);
            String rhelVersion = nvrParser.extractRhelVersion(packageNvr);

            if (packageName == null || version == null || release == null || rhelVersion == null) {
                throw new IllegalStateException(
                        "Could not extract required package components from valid NVR: " + packageNvr);
            }

            String baseUrl = String.format(UrlConstants.SOURCE_CODE_BASE_URL_TEMPLATE, rhelVersion);
            return baseUrl + packageName + "/" + version + "/" + release + "/src/" + packageNvr
                    + UrlConstants.SOURCE_CODE_SUFFIX;
        }

        @Override
        protected String getOperationName() {
            return "source code URL";
        }
    }

    /**
     * Operation for inferring project names.
     */
    public static class ProjectNameOperation extends UrlInferenceOperation {

        public ProjectNameOperation(NvrParser nvrParser, Logger logger) {
            super(nvrParser, logger);
        }

        @Override
        protected String performInference(String packageNvr) {
            String projectName = nvrParser.extractPackageName(packageNvr);
            if (projectName == null) {
                throw new IllegalStateException("Could not extract project name from valid NVR: " + packageNvr);
            }
            return projectName;
        }

        @Override
        protected String getOperationName() {
            return "project name";
        }
    }

    /**
     * Operation for inferring project versions.
     */
    public static class ProjectVersionOperation extends UrlInferenceOperation {

        public ProjectVersionOperation(NvrParser nvrParser, Logger logger) {
            super(nvrParser, logger);
        }

        @Override
        protected String performInference(String packageNvr) {
            String version = nvrParser.extractVersion(packageNvr);
            String release = nvrParser.extractRelease(packageNvr);

            if (version == null || release == null) {
                throw new IllegalStateException("Could not extract version/release from valid NVR: " + packageNvr);
            }

            // Extract the release part before ".el" (e.g., "9.el10" -> "9")
            String releaseWithoutEl = release.split("\\.el")[0];
            return version + "-" + releaseWithoutEl;
        }

        @Override
        protected String getOperationName() {
            return "project version";
        }
    }

    /**
     * Operation for inferring package names (identical to project name).
     */
    public static class PackageNameOperation extends UrlInferenceOperation {

        public PackageNameOperation(NvrParser nvrParser, Logger logger) {
            super(nvrParser, logger);
        }

        @Override
        protected String performInference(String packageNvr) {
            String packageName = nvrParser.extractPackageName(packageNvr);
            if (packageName == null) {
                throw new IllegalStateException("Could not extract package name from valid NVR: " + packageNvr);
            }
            return packageName;
        }

        @Override
        protected String getOperationName() {
            return "package name";
        }
    }
}
