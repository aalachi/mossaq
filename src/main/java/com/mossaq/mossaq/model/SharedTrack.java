package com.mossaq.mossaq.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "shared_tracks")
public class SharedTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID senderId;
    private UUID recipientId;
    private UUID trackId;
    private Long sharedAt;

    public SharedTrack() {}

    public SharedTrack(UUID senderId, UUID recipientId, UUID trackId) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.trackId = trackId;
        this.sharedAt = System.currentTimeMillis();
    }

    public UUID getId() { return id; }
    
    public UUID getSenderId() { return senderId; }
    public void setSenderId(UUID senderId) { this.senderId = senderId; }

    public UUID getRecipientId() { return recipientId; }
    public void setRecipientId(UUID recipientId) { this.recipientId = recipientId; }

    public UUID getTrackId() { return trackId; }
    public void setTrackId(UUID trackId) { this.trackId = trackId; }

    public Long getSharedAt() { return sharedAt; }
    public void setSharedAt(Long sharedAt) { this.sharedAt = sharedAt; }
}