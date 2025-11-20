package com.redhat.sast.api.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.model.OshSchedulerCursor;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@QuarkusTest
@TestProfile(com.redhat.sast.api.config.TestProfile.class)
@DisplayName("OSH Scheduler Cursor Repository Tests")
class OshSchedulerCursorRepositoryTest {

    @Inject
    OshSchedulerCursorRepository repository;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing cursor records before each test
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should return empty when no cursor exists")
    void shouldReturnEmptyWhenNoCursorExists() {
        Optional<OshSchedulerCursor> cursor = repository.getCurrentCursor();

        assertTrue(cursor.isEmpty());
    }

    @Test
    @DisplayName("Should create and retrieve cursor successfully")
    @Transactional
    void shouldCreateAndRetrieveCursorSuccessfully() {
        String testToken = "osh_scan_12345";
        LocalDateTime testTimestamp = LocalDateTime.now();

        repository.updateCursor(testToken, testTimestamp);

        Optional<OshSchedulerCursor> cursor = repository.getCurrentCursor();

        assertTrue(cursor.isPresent());
        assertEquals(testToken, cursor.get().getLastSeenToken());
        assertEquals(testTimestamp, cursor.get().getLastSeenTimestamp());
        assertNotNull(cursor.get().getUpdatedAt());
        assertNotNull(cursor.get().getId());
    }

    @Test
    @DisplayName("Should update existing cursor when called multiple times")
    @Transactional
    void shouldUpdateExistingCursorWhenCalledMultipleTimes() {
        // First update
        String firstToken = "osh_scan_11111";
        LocalDateTime firstTimestamp = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

        repository.updateCursor(firstToken, firstTimestamp);
        Optional<OshSchedulerCursor> firstCursor = repository.getCurrentCursor();
        assertTrue(firstCursor.isPresent());
        Long cursorId = firstCursor.get().getId();

        // Second update - should update same record, not create new one
        String secondToken = "osh_scan_22222";
        LocalDateTime secondTimestamp = LocalDateTime.of(2024, 1, 2, 15, 30, 0);

        repository.updateCursor(secondToken, secondTimestamp);
        Optional<OshSchedulerCursor> secondCursor = repository.getCurrentCursor();

        assertTrue(secondCursor.isPresent());
        assertEquals(cursorId, secondCursor.get().getId()); // Same ID - updated, not created
        assertEquals(secondToken, secondCursor.get().getLastSeenToken());
        assertEquals(secondTimestamp, secondCursor.get().getLastSeenTimestamp());

        // Verify only one cursor record exists
        assertEquals(1, repository.count());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    @Transactional
    void shouldHandleNullValuesGracefully() {
        repository.updateCursor(null, null);

        Optional<OshSchedulerCursor> cursor = repository.getCurrentCursor();

        assertTrue(cursor.isPresent());
        assertNull(cursor.get().getLastSeenToken());
        assertNull(cursor.get().getLastSeenTimestamp());
        assertNotNull(cursor.get().getUpdatedAt()); // This should still be set by @PrePersist
    }

    @Test
    @DisplayName("Should automatically set updatedAt timestamp")
    @Transactional
    void shouldAutomaticallySetUpdatedAtTimestamp() {
        LocalDateTime beforeUpdate = LocalDateTime.now();

        repository.updateCursor("test_token", LocalDateTime.now());

        Optional<OshSchedulerCursor> cursor = repository.getCurrentCursor();

        assertTrue(cursor.isPresent());
        assertNotNull(cursor.get().getUpdatedAt());
        assertTrue(cursor.get().getUpdatedAt().isAfter(beforeUpdate)
                || cursor.get().getUpdatedAt().isEqual(beforeUpdate));
    }

    @Test
    @DisplayName("Should update updatedAt timestamp on subsequent updates")
    @Transactional
    void shouldUpdateUpdatedAtTimestampOnSubsequentUpdates() {
        LocalDateTime firstTimestamp = LocalDateTime.now();
        LocalDateTime secondTimestamp = firstTimestamp.plusSeconds(1);

        repository.updateCursor("token1", firstTimestamp);
        Optional<OshSchedulerCursor> firstCursor = repository.getCurrentCursor();
        LocalDateTime firstUpdatedAt = firstCursor.get().getUpdatedAt();

        repository.updateCursor("token2", secondTimestamp);
        Optional<OshSchedulerCursor> secondCursor = repository.getCurrentCursor();
        LocalDateTime secondUpdatedAt = secondCursor.get().getUpdatedAt();

        assertTrue(secondUpdatedAt.isAfter(firstUpdatedAt));
    }

    @Test
    @DisplayName("Should persist cursor across separate method calls")
    @Transactional
    void shouldPersistCursorAcrossSeparateMethodCalls() {
        repository.updateCursor("persistent_token", LocalDateTime.now());

        Optional<OshSchedulerCursor> cursor = repository.getCurrentCursor();

        assertTrue(cursor.isPresent());
        assertEquals("persistent_token", cursor.get().getLastSeenToken());
    }
}
