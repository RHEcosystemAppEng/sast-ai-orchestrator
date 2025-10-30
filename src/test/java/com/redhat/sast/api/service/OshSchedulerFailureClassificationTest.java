package com.redhat.sast.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.enums.OshFailureReason;
import com.redhat.sast.api.startup.OshScheduler;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;

/**
 * Tests for OshScheduler failure classification logic.
 *
 * Tests the classifyFailure method to ensure accurate categorization
 * of different types of exceptions for retry processing.
 */
@QuarkusTest
@TestProfile(com.redhat.sast.api.config.TestProfile.class)
@DisplayName("OSH Scheduler Failure Classification Tests")
class OshSchedulerFailureClassificationTest {

    @Inject
    OshScheduler oshScheduler;

    @Test
    @DisplayName("Should classify network exceptions as OSH metadata network error")
    void testNetworkExceptionClassification() {
        ConnectException connectException = new ConnectException("Connection refused");
        OshFailureReason result = classifyFailure(connectException);
        assertEquals(OshFailureReason.OSH_METADATA_NETWORK_ERROR, result);

        SocketTimeoutException timeoutException = new SocketTimeoutException("Connection timed out");
        result = classifyFailure(timeoutException);
        assertEquals(OshFailureReason.OSH_METADATA_NETWORK_ERROR, result);

        IOException ioException = new IOException("Network error");
        result = classifyFailure(ioException);
        assertEquals(OshFailureReason.OSH_METADATA_NETWORK_ERROR, result);
    }

    @Test
    @DisplayName("Should classify network exceptions by message content")
    void testNetworkExceptionClassificationByMessage() {
        RuntimeException connectionError = new RuntimeException("connection failed");
        OshFailureReason result = classifyFailure(connectionError);
        assertEquals(OshFailureReason.OSH_METADATA_NETWORK_ERROR, result);

        RuntimeException timeoutError = new RuntimeException("Request timeout occurred");
        result = classifyFailure(timeoutError);
        assertEquals(OshFailureReason.OSH_METADATA_NETWORK_ERROR, result);

        RuntimeException upperCaseError = new RuntimeException("CONNECTION ERROR");
        result = classifyFailure(upperCaseError);
        assertEquals(OshFailureReason.OSH_METADATA_NETWORK_ERROR, result);
    }

    @Test
    @DisplayName("Should classify data validation exceptions")
    void testDataValidationExceptionClassification() {
        IllegalArgumentException argException = new IllegalArgumentException("Invalid scan data");
        OshFailureReason result = classifyFailure(argException);
        assertEquals(OshFailureReason.SCAN_DATA_ERROR, result);

        IllegalStateException stateException = new IllegalStateException("Invalid state");
        result = classifyFailure(stateException);
        assertEquals(OshFailureReason.SCAN_DATA_ERROR, result);

        RuntimeException invalidError = new RuntimeException("invalid component name");
        result = classifyFailure(invalidError);
        assertEquals(OshFailureReason.SCAN_DATA_ERROR, result);

        RuntimeException missingError = new RuntimeException("missing required field");
        result = classifyFailure(missingError);
        assertEquals(OshFailureReason.SCAN_DATA_ERROR, result);
    }

    @Test
    @DisplayName("Should classify database exceptions")
    void testDatabaseExceptionClassification() {
        PersistenceException persistenceException = new PersistenceException("Database error");
        OshFailureReason result = classifyFailure(persistenceException);
        assertEquals(OshFailureReason.DATABASE_ERROR, result);

        RuntimeException databaseError = new RuntimeException("database error occurred");
        result = classifyFailure(databaseError);
        assertEquals(OshFailureReason.DATABASE_ERROR, result);

        RuntimeException constraintError = new RuntimeException("constraint violation");
        result = classifyFailure(constraintError);
        assertEquals(OshFailureReason.DATABASE_ERROR, result);
    }

    @Test
    @DisplayName("Should classify API-related exceptions")
    void testApiExceptionClassification() {
        RuntimeException httpError = new RuntimeException("HTTP 500 error");
        OshFailureReason result = classifyFailure(httpError);
        assertEquals(OshFailureReason.OSH_API_ERROR, result);

        RuntimeException apiError = new RuntimeException("api endpoint not found");
        result = classifyFailure(apiError);
        assertEquals(OshFailureReason.OSH_API_ERROR, result);

        RuntimeException oshError = new RuntimeException("osh service unavailable");
        result = classifyFailure(oshError);
        assertEquals(OshFailureReason.OSH_API_ERROR, result);
    }

    @Test
    @DisplayName("Should classify job creation exceptions")
    void testJobCreationExceptionClassification() {
        RuntimeException jobError = new RuntimeException("job creation failed");
        OshFailureReason result = classifyFailure(jobError);
        assertEquals(OshFailureReason.JOB_CREATION_ERROR, result);

        RuntimeException creationError = new RuntimeException("creation process failed");
        result = classifyFailure(creationError);
        assertEquals(OshFailureReason.JOB_CREATION_ERROR, result);
    }

    @Test
    @DisplayName("Should classify null exception as unknown")
    void testNullExceptionClassification() {
        OshFailureReason result = classifyFailure(null);
        assertEquals(OshFailureReason.UNKNOWN_ERROR, result);
    }

    @Test
    @DisplayName("Should classify exception with null message as unknown")
    void testNullMessageExceptionClassification() {
        RuntimeException exceptionWithNullMessage = new RuntimeException((String) null);
        OshFailureReason result = classifyFailure(exceptionWithNullMessage);
        assertEquals(OshFailureReason.UNKNOWN_ERROR, result);
    }

    @Test
    @DisplayName("Should classify unrecognized exceptions as unknown")
    void testUnrecognizedExceptionClassification() {
        RuntimeException unknownException = new RuntimeException("Something completely unexpected happened");
        OshFailureReason result = classifyFailure(unknownException);
        assertEquals(OshFailureReason.UNKNOWN_ERROR, result);

        RuntimeException emptyMessageException = new RuntimeException("");
        result = classifyFailure(emptyMessageException);
        assertEquals(OshFailureReason.UNKNOWN_ERROR, result);
    }

    @Test
    @DisplayName("Should handle case insensitive message matching")
    void testCaseInsensitiveMessageMatching() {
        String[] testMessages = {
            "CONNECTION failed",
            "Connection Failed",
            "connection FAILED",
            "TIMEOUT occurred",
            "Timeout Occurred",
            "timeout OCCURRED"
        };

        for (String message : testMessages) {
            RuntimeException exception = new RuntimeException(message);
            OshFailureReason result = classifyFailure(exception);
            assertEquals(
                    OshFailureReason.OSH_METADATA_NETWORK_ERROR,
                    result,
                    "Should classify message as network error: " + message);
        }
    }

    @Test
    @DisplayName("Should handle partial message matching")
    void testPartialMessageMatching() {
        RuntimeException timeoutInMiddle = new RuntimeException("Request failed due to timeout while connecting");
        OshFailureReason result = classifyFailure(timeoutInMiddle);
        assertEquals(OshFailureReason.OSH_METADATA_NETWORK_ERROR, result);

        RuntimeException invalidInMiddle = new RuntimeException("The provided invalid data caused processing to fail");
        result = classifyFailure(invalidInMiddle);
        assertEquals(OshFailureReason.SCAN_DATA_ERROR, result);
    }

    private OshFailureReason classifyFailure(Exception exception) {
        try {
            var method = OshScheduler.class.getDeclaredMethod("classifyFailure", Exception.class);
            method.setAccessible(true);
            return (OshFailureReason) method.invoke(oshScheduler, exception);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke classifyFailure method", e);
        }
    }
}
