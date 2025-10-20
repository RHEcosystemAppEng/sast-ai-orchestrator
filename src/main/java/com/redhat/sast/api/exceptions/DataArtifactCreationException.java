package com.redhat.sast.api.exceptions;

/**
 * Exception thrown when data artifact creation fails.
 */
public class DataArtifactCreationException extends RuntimeException {

    public DataArtifactCreationException(String message) {
        super(message);
    }

    public DataArtifactCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
