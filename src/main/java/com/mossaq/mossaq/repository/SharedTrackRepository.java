package com.mossaq.mossaq.repository;

import com.mossaq.mossaq.model.SharedTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SharedTrackRepository extends JpaRepository<SharedTrack, UUID> {
    List<SharedTrack> findByRecipientIdOrderBySharedAtDesc(UUID recipientId);
}