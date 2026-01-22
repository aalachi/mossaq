package com.mossaq.mossaq.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "track_likes")
public class TrackLike {

    @Id
    private UUID id;
    private UUID trackId;
    private UUID userId;

    public TrackLike() {
    }

    public TrackLike(UUID trackId, UUID userId) {
        this.id = UUID.randomUUID();
        this.trackId = trackId;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTrackId() {
        return trackId;
    }

    public UUID getUserId() {
        return userId;
    }
}