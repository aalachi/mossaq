package com.mossaq.mossaq.services;

import com.mossaq.mossaq.model.Track;
import com.mossaq.mossaq.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class TrackService {

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
    private final TrackRepository trackRepository;

    @Autowired
    public TrackService(TrackRepository trackRepository) throws IOException {
        this.trackRepository = trackRepository;
        Files.createDirectories(this.fileStorageLocation);
    }

    public Track uploadTrack(MultipartFile file, String title, String artist) throws IOException {
        var fileName = file.getOriginalFilename();
        var targetLocation = this.fileStorageLocation.resolve(fileName);
        
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        var newTrack = new Track(title, artist, fileName, file.getContentType());
        newTrack.setAudioFilePath(targetLocation.toString());
        return trackRepository.save(newTrack);
    }

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public Optional<Track> getTrackById(java.util.UUID id) {
        return trackRepository.findById(id);
    }

    public Path getTrackPath(String filename) {
        return fileStorageLocation.resolve(filename);
    }
}