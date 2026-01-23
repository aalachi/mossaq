package com.mossaq.mossaq.controllers;

import com.mossaq.mossaq.model.User;
import com.mossaq.mossaq.repository.UserRepository;
import com.mossaq.mossaq.services.TrackService;
import com.mossaq.mossaq.services.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    
    private final TrackService trackService;
    private final UserService userService;
    private final UserRepository userRepository;

    public HomeController(TrackService trackService, UserService userService, UserRepository userRepository) {
        this.trackService = trackService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String email, @RequestParam String password , @RequestParam String username) {
        try {
            userService.registerUser(email, password, username);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Username")) {
                return "redirect:/register?error=username";
            }
            if (e.getMessage().contains("Weak")) {
                return "redirect:/register?error=password";
            }
            return "redirect:/register?error=email";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("bio", user.getBio());
        model.addAttribute("avatar", user.getAvatarFilename());
        // On convertit l'UUID en String car dans Track c'est stocké en String pour l'instant
        model.addAttribute("tracks", trackService.getTracksByUserId(user.getUuid().toString()));
        
        return "profile";
    }

    @GetMapping("/settings")
    public String settings(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("email", user.getEmail());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("bio", user.getBio());
        model.addAttribute("avatar", user.getAvatarFilename());
        return "settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam String username, @RequestParam String email, 
                                 @RequestParam(required = false) String password, 
                                 @RequestParam(required = false) String bio,
                                 @RequestParam(required = false) MultipartFile avatar,
                                 Principal principal) throws IOException {
        userService.updateUser(principal.getName(), username, email, password, bio, avatar);
        // Si l'email change, il faudrait techniquement déconnecter l'utilisateur, mais restons simples.
        return "redirect:/profile";
    }

    @PostMapping("/settings/avatar/delete")
    public String deleteAvatar(Principal principal) throws IOException {
        userService.deleteAvatar(principal.getName());
        return "redirect:/settings";
    }

    @GetMapping("/avatar/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            Path file = Paths.get("uploads").resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok().body(resource);
            }
        } catch (MalformedURLException e) {}
        return ResponseEntity.notFound().build();
    }
}
