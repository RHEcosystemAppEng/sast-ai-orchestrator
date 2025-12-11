package com.redhat.sast.api.repository.osh;

import java.time.Instant;
import java.util.Optional;

import com.redhat.sast.api.model.OshSchedulerCursor;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OshSchedulerCursorRepository implements PanacheRepository<OshSchedulerCursor> {

    public Optional<OshSchedulerCursor> getCurrentCursor() {
        return findAll().firstResultOptional();
    }

    @Transactional
    public void updateCursor(String lastSeenToken, Instant timestamp) {
        OshSchedulerCursor cursor = getCurrentCursor().orElseGet(OshSchedulerCursor::new);
        cursor.setLastSeenToken(lastSeenToken);
        cursor.setLastSeenTimestamp(timestamp);
        persist(cursor);
    }
}
