package com.mossaq.mossaq.repository;

import com.mossaq.mossaq.model.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, UUID> {
    List<PlaylistTrack> findByUserId(UUID userId);
    Optional<PlaylistTrack> findByUserIdAndTrackId(UUID userId, UUID trackId);
}