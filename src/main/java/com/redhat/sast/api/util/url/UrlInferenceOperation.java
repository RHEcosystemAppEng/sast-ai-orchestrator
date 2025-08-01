package com.redhat.sast.api.util.url;

import org.jboss.logging.Logger;

import com.redhat.sast.api.exceptions.InvalidNvrException;

/**
 * Abstract base class for URL inference operations using the Template Method pattern.
 * This class defines the common algorithm for validating NVR inputs and performing
 * inference operations while allowing concrete implementations to customize the
 * specific inference logic.
 */
public abstract class UrlInferenceOperation {

    protected final NvrParser nvrParser;
    protected final Logger logger;

    /**
     * Constructs a new UrlInferenceOperation with the specified dependencies.
     *
     * @param nvrParser the NVR parser for extracting package components
     * @param logger the logger for recording operations and errors
     */
    protected UrlInferenceOperation(NvrParser nvrParser, Logger logger) {
        this.nvrParser = nvrParser;
        this.logger = logger;
    }

    /**
     * Template method that defines the algorithm for URL inference operations.
     * This method validates the input NVR and delegates to concrete implementations
     * for the specific inference logic.
     *
     * @param packageNvr the package NVR to process
     * @return the inferred result
     * @throws InvalidNvrException if the NVR is invalid or inference fails
     */
    public final String execute(String packageNvr) {
        validateNvr(packageNvr);

        try {
            return performInference(packageNvr);
        } catch (Exception e) {
            String errorMsg = String.format(
                    "Failed to infer %s from NVR '%s': %s", getOperationName(), packageNvr, e.getMessage());
            logger.error(errorMsg, e);
            throw new InvalidNvrException(errorMsg, e);
        }
    }

    /**
     * Validates the input NVR and throws detailed exceptions for specific validation failures.
     *
     * @param packageNvr the package NVR to validate
     * @throws InvalidNvrException if the NVR is invalid
     */
    private void validateNvr(String packageNvr) {
        if (packageNvr == null) {
            throw new InvalidNvrException("Package NVR cannot be null for " + getOperationName());
        }
        if (packageNvr.trim().isEmpty()) {
            throw new InvalidNvrException("Package NVR cannot be empty for " + getOperationName());
        }
        if (!nvrParser.isValidNvr(packageNvr)) {
            throw new InvalidNvrException("Invalid NVR format '" + packageNvr + "' for " + getOperationName());
        }
    }

    /**
     * Hook method to be implemented by concrete classes to perform the specific
     * inference logic.
     *
     * @param packageNvr the validated package NVR
     * @return the inferred result
     * @throws Exception if inference fails for any reason
     */
    protected abstract String performInference(String packageNvr) throws Exception;

    /**
     * Hook method to be implemented by concrete classes to provide a descriptive
     * name for the operation, used in error messages and logging.
     *
     * @return the operation name
     */
    protected abstract String getOperationName();
}
