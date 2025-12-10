package com.redhat.sast.api.exceptions;

public class SastAiConfigException extends RuntimeException {
    public SastAiConfigException(String message) {
        super(message);
    }

    public SastAiConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
