package com.mossaq.mossaq.repository;

import com.mossaq.mossaq.model.TrackLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TrackLikeRepository extends JpaRepository<TrackLike, UUID> {
    Optional<TrackLike> findByTrackIdAndUserId(UUID trackId, UUID userId);
}