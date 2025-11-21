package com.redhat.sast.api.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "osh_scheduler_cursor")
@Data
@NoArgsConstructor
public class OshSchedulerCursor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "last_seen_token", length = 255)
    private String lastSeenToken;

    @Column(name = "last_seen_timestamp")
    private Instant lastSeenTimestamp;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = Instant.now();
    }
}
