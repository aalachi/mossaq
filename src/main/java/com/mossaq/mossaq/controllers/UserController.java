package com.mossaq.mossaq.controllers;

import com.mossaq.mossaq.model.Friendship;
import com.mossaq.mossaq.model.User;
import com.mossaq.mossaq.repository.FriendshipRepository;
import com.mossaq.mossaq.repository.UserRepository;
import com.mossaq.mossaq.services.TrackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.UUID;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final TrackService trackService;
    private final FriendshipRepository friendshipRepository;

    public UserController(UserRepository userRepository, TrackService trackService, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.trackService = trackService;
        this.friendshipRepository = friendshipRepository;
    }

    @GetMapping("/profile/search")
    public String searchUsers(@RequestParam("query") String query, Model model, Principal principal) {
        // 1. Populate current user profile data (so the sidebar/header remains correct)
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                model.addAttribute("username", user.getUsername());
                model.addAttribute("email", user.getEmail());
                model.addAttribute("bio", user.getBio());
                model.addAttribute("avatar", user.getAvatarFilename());
                
                // Fetch current user's tracks for the profile view
                model.addAttribute("tracks", trackService.getTracksByUserId(user.getUuid().toString()));
            });
        }

        // 2. Perform Search
        model.addAttribute("searchResults", userRepository.findByUsernameContainingIgnoreCase(query));
        model.addAttribute("searchQuery", query);
        
        return "profile";
    }

    @GetMapping("/user/{id}")
    public String publicProfile(@PathVariable UUID id, Model model, Principal principal) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // If viewing own profile, redirect to main profile
        if (principal != null && user.getEmail().equals(principal.getName())) {
            return "redirect:/profile";
        }

        model.addAttribute("user", user);
        
        // Fetch tracks for this specific user
        var userTracks = trackService.getTracksByUserId(user.getUuid().toString());
        
        model.addAttribute("tracks", userTracks);

        // Check Friendship Status
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(currentUser -> {
                String status = "NONE";
                if (friendshipRepository.existsByRequesterIdAndAddresseeId(currentUser.getUuid(), user.getUuid())) {
                    status = "REQUEST_SENT";
                } else if (friendshipRepository.findAllFriends(currentUser.getUuid()).stream()
                        .anyMatch(f -> f.getRequesterId().equals(user.getUuid()) || f.getAddresseeId().equals(user.getUuid()))) {
                    status = "FRIENDS";
                }
                model.addAttribute("friendshipStatus", status);
            });
        }

        return "public_profile";
    }
}