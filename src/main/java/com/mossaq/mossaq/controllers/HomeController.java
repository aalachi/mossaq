package com.mossaq.mossaq.controllers;

import com.mossaq.mossaq.services.TrackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    private final TrackService trackService;

    public HomeController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
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
