package com.mossaq.mossaq.controllers;

import com.mossaq.mossaq.services.TrackService;
import com.mossaq.mossaq.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    
    private final TrackService trackService;
    private final UserService userService;

    public HomeController(TrackService trackService, UserService userService) {
        this.trackService = trackService;
        this.userService = userService;
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

    @GetMapping("/main")
    public String main(Model model) {
        model.addAttribute("tracks", trackService.getAllTracks());
        return "dashboard";
    }
}
