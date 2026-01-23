package com.mossaq.mossaq.controllers;

import com.mossaq.mossaq.services.TrackService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
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
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Controller
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping({"/", "/main"})
    public String dashboard(Model model) {
        model.addAttribute("tracks", trackService.getAllTracks());
        return "dashboard";
    }

    @GetMapping("/track")
    public String track(@RequestParam(value = "uuid", required = false) java.util.UUID uuid, Model model, Principal principal) {
        if (uuid != null) {
            trackService.getTrackById(uuid).ifPresent(track -> {
                model.addAttribute("track", track);
                if (principal != null) {
                    model.addAttribute("inPlaylist", trackService.isTrackInPlaylist(uuid, principal.getName()));
                } else {
                    model.addAttribute("inPlaylist", false);
                }
            });
        }
        return "track";
    }

    @PostMapping("/track/upload")
    public String uploadTrack(@RequestParam("file") MultipartFile file,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              @RequestParam("title") String title,
<<<<<<< HEAD
                              @RequestParam("artist") String artist,
                              java.security.Principal principal,
                              Model model) throws IOException {
        trackService.uploadTrack(file, image, title, artist, principal.getName());
        model.addAttribute("message", "Track uploaded successfully!");
        return "redirect:/profile";
=======
                              Model model,
                              Principal principal) throws IOException {
        String userEmail = (principal != null) ? principal.getName() : null;
        trackService.uploadTrack(file, image, title, userEmail);
        model.addAttribute("message", "Track uploaded successfully!");
        return "redirect:/main";
>>>>>>> e7620ccbe5f892db909569fde751a909398174db
    }

    @GetMapping("/track/{id}/stream")
    public ResponseEntity<Resource> streamTrack(@PathVariable java.util.UUID id) {
        var trackOpt = trackService.getTrackById(id);
        if (trackOpt.isEmpty()) return ResponseEntity.notFound().build();

        var track = trackOpt.get();
        try {
            Path filePath = trackService.getTrackPath(track.getFilename());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, track.getContentType())
                        .body(resource);
            }
        } catch (MalformedURLException e) {}
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/track/{id}/art")
    public ResponseEntity<Resource> getTrackArt(@PathVariable java.util.UUID id) {
        var trackOpt = trackService.getTrackById(id);
        if (trackOpt.isEmpty()) return serveDefaultArt();

        var track = trackOpt.get();
        if (track.getImageFilePath() == null) {
            return serveDefaultArt();
        }

        try {
            Path filePath = trackService.getImagePath(track.getImageFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .body(resource);
            }
        } catch (MalformedURLException e) {}
        
        return serveDefaultArt();
    }

    private ResponseEntity<Resource> serveDefaultArt() {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"200\" viewBox=\"0 0 200 200\"><rect width=\"200\" height=\"200\" fill=\"#333\"/><text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" fill=\"#555\" font-family=\"sans-serif\" font-size=\"24\">No Art</text></svg>";
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(new org.springframework.core.io.ByteArrayResource(svg.getBytes(StandardCharsets.UTF_8)));
    }

    @PostMapping("/track/{id}/like")
    public ResponseEntity<java.util.Map<String, Object>> likeTrack(@PathVariable java.util.UUID id, java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        java.util.Map<String, Object> result = trackService.likeTrack(id, principal.getName());
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

<<<<<<< HEAD
    @PostMapping("/track/{id}/delete")
    public String deleteTrack(@PathVariable java.util.UUID id, java.security.Principal principal) throws IOException {
        trackService.deleteTrack(id, principal.getName());
        return "redirect:/profile";
=======
    @GetMapping("/library")
    public String library(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("tracks", trackService.getPlaylistTracks(principal.getName()));
        } else {
            model.addAttribute("tracks", java.util.Collections.emptyList());
        }
        model.addAttribute("sectionTitle", "My Playlist");
        model.addAttribute("activeTab", "library");
        return "dashboard";
    }

    @PostMapping("/track/{id}/playlist")
    public ResponseEntity<Void> addToPlaylist(@PathVariable java.util.UUID id, java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        boolean success = trackService.addToPlaylist(id, principal.getName());
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
>>>>>>> e7620ccbe5f892db909569fde751a909398174db
    }
}