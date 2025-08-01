package com.redhat.sast.api.exceptions;

/**
 * Exception thrown when an invalid NVR (Name-Version-Release) is encountered
 * during URL inference operations.
 */
public class InvalidNvrException extends RuntimeException {

    /**
     * Constructs a new InvalidNvrException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidNvrException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidNvrException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public InvalidNvrException(String message, Throwable cause) {
        super(message, cause);
    }
}
