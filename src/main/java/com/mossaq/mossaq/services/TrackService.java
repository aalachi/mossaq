package com.mossaq.mossaq.services;

import com.mossaq.mossaq.model.Track;
import com.mossaq.mossaq.model.TrackLike;
import com.mossaq.mossaq.model.User;
import com.mossaq.mossaq.repository.TrackRepository;
import com.mossaq.mossaq.repository.TrackLikeRepository;
import com.mossaq.mossaq.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final TrackLikeRepository trackLikeRepository;

    public TrackService(TrackRepository trackRepository, 
                        UserRepository userRepository,
                        TrackLikeRepository trackLikeRepository) throws IOException {
        this.trackRepository = trackRepository;
        this.userRepository = userRepository;
        this.trackLikeRepository = trackLikeRepository;
        Files.createDirectories(this.fileStorageLocation);
    }

    public Track uploadTrack(MultipartFile file, MultipartFile image, String title, String artist) throws IOException {
        var fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        var targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        String imageFileName = null;
        if (image != null && !image.isEmpty()) {
            imageFileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            var imageTarget = this.fileStorageLocation.resolve(imageFileName);
            Files.copy(image.getInputStream(), imageTarget, StandardCopyOption.REPLACE_EXISTING);
        }

        // Using "Anonymous" for userId as placeholder until auth is fully integrated in upload
        var newTrack = new Track(title, artist, "Anonymous", fileName, file.getContentType(), targetLocation.toString(), imageFileName);
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
    
    public Path getImagePath(String filename) {
        return fileStorageLocation.resolve(filename);
    }

    public List<Track> getTracksByArtist(String artist) {
        return trackRepository.findByArtist(artist);
    }

    public java.util.Map<String, Object> likeTrack(java.util.UUID trackId, String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return null;
        User user = userOpt.get();

        Optional<Track> trackOpt = trackRepository.findById(trackId);
        if (trackOpt.isEmpty()) return null;
        Track track = trackOpt.get();

        Optional<TrackLike> existingLike = trackLikeRepository.findByTrackIdAndUserId(trackId, user.getUuid());
        int currentCount = track.getLikeCount() == null ? 0 : track.getLikeCount();
        boolean isLiked;

        if (existingLike.isPresent()) {
            trackLikeRepository.delete(existingLike.get());
            track.setLikeCount(Math.max(0, currentCount - 1));
            isLiked = false;
        } else {
            trackLikeRepository.save(new TrackLike(trackId, user.getUuid()));
            track.setLikeCount(currentCount + 1);
            isLiked = true;
        }
        
        trackRepository.save(track);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("count", track.getLikeCount());
        result.put("liked", isLiked);
        return result;
    }
}