package com.mossaq.mossaq.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "friendships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"requesterId", "addresseeId"})
})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID requesterId;
    private UUID addresseeId;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    public enum FriendshipStatus {
        PENDING, ACCEPTED
    }

    public Friendship() {}

    public Friendship(UUID requesterId, UUID addresseeId, FriendshipStatus status) {
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getRequesterId() { return requesterId; }
    public UUID getAddresseeId() { return addresseeId; }
    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }
}