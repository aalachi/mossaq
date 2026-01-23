package com.mossaq.mossaq.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "playlist_tracks")
public class PlaylistTrack {

    @Id
    private UUID id;
    private UUID userId;
    private UUID trackId;
    private Long addedAt;

    public PlaylistTrack() {
    }

    public PlaylistTrack(UUID userId, UUID trackId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.trackId = trackId;
        this.addedAt = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTrackId() {
        return trackId;
    }

    public Long getAddedAt() {
        return addedAt;
    }
}