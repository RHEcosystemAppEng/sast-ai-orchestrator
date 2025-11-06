package com.redhat.sast.api.exceptions;

/**
 * Exception thrown when DVC operations fail.
 * This includes failures in fetching data from DVC repositories,
 * parsing DVC YAML files, or DVC command execution errors.
 */
public class DvcException extends RuntimeException {

    /**
     * Constructs a new DvcException with the specified detail message.
     *
     * @param message the detail message
     */
    public DvcException(String message) {
        super(message);
    }

    /**
     * Constructs a new DvcException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public DvcException(String message, Throwable cause) {
        super(message, cause);
    }
}
