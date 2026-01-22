package com.mossaq.mossaq.repository;

import com.mossaq.mossaq.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
}