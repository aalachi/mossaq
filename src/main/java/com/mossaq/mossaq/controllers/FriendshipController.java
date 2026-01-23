package com.mossaq.mossaq.controllers;

import com.mossaq.mossaq.model.Friendship;
import com.mossaq.mossaq.model.User;
import com.mossaq.mossaq.repository.FriendshipRepository;
import com.mossaq.mossaq.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.UUID;

@Controller
public class FriendshipController {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipController(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/friend/add/{id}")
    public String sendRequest(@PathVariable UUID id, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        
        if (currentUser.getUuid().equals(id)) {
             return "redirect:/user/" + id;
        }

        if (friendshipRepository.existsByRequesterIdAndAddresseeId(currentUser.getUuid(), id)) {
             redirectAttributes.addFlashAttribute("message", "Request already sent.");
        } else if (friendshipRepository.existsByRequesterIdAndAddresseeId(id, currentUser.getUuid())) {
             redirectAttributes.addFlashAttribute("message", "This user already sent you a request. Check your profile!");
        } else {
            friendshipRepository.save(new Friendship(currentUser.getUuid(), id, Friendship.FriendshipStatus.PENDING));
            redirectAttributes.addFlashAttribute("message", "Friend request sent!");
        }
        return "redirect:/user/" + id;
    }

    @PostMapping("/friend/accept/{id}")
    public String acceptRequest(@PathVariable UUID id, Principal principal) {
        friendshipRepository.findById(id).ifPresent(friendship -> {
            User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
            if (friendship.getAddresseeId().equals(currentUser.getUuid())) {
                friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
                friendshipRepository.save(friendship);
            }
        });
        return "redirect:/profile";
    }
    
    @PostMapping("/friend/decline/{id}")
    public String declineRequest(@PathVariable UUID id, Principal principal) {
        friendshipRepository.findById(id).ifPresent(friendship -> {
            User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
            if (friendship.getAddresseeId().equals(currentUser.getUuid())) {
                friendshipRepository.delete(friendship);
            }
        });
        return "redirect:/profile";
    }
}