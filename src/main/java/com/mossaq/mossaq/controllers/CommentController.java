package com.mossaq.mossaq.controllers;

import com.mossaq.mossaq.model.Comment;
import com.mossaq.mossaq.model.User;
import com.mossaq.mossaq.repository.CommentRepository;
import com.mossaq.mossaq.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.UUID;

@Controller
public class CommentController {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentController(CommentRepository commentRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/track/{trackId}/comment")
    public String addComment(@PathVariable UUID trackId, @RequestParam String content, Principal principal) { // Spring Security Principal
        String author = (principal != null) ? principal.getName() : "Anonymous";
        if (principal != null) {
            User user = userRepository.findByEmail(author).orElse(null);
            if (user != null) {
                author = user.getUsername();
            }
        }
        Comment comment = new Comment(content, author, trackId);
        comment.setTimestamp(System.currentTimeMillis());
        commentRepository.save(comment);
        return "redirect:/track?uuid=" + trackId;
    }
}