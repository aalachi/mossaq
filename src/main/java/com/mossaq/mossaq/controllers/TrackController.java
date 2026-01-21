package com.mossaq.mossaq.controllers;

import com.mossaq.mossaq.services.TrackService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
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

@Controller
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping("/track")
    public String track(@RequestParam(value = "uuid", required = false) java.util.UUID uuid, Model model) {
        if (uuid != null) {
            trackService.getTrackById(uuid).ifPresent(track -> model.addAttribute("track", track));
        }
        return "track";
    }

    @PostMapping("/track/upload")
    public String uploadTrack(@RequestParam("file") MultipartFile file,
                              @RequestParam("title") String title,
                              @RequestParam("artist") String artist,
                              Model model) throws IOException {
        trackService.uploadTrack(file, title, artist);
        model.addAttribute("message", "Track uploaded successfully!");
        return "track";
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
}