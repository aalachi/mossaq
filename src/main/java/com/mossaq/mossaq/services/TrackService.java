package com.mossaq.mossaq.services;

import com.mossaq.mossaq.model.PlaylistTrack;
import com.mossaq.mossaq.model.Track;
import com.mossaq.mossaq.model.TrackLike;
import com.mossaq.mossaq.model.User;
import com.mossaq.mossaq.repository.PlaylistTrackRepository;
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
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class TrackService {

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final TrackLikeRepository trackLikeRepository;
    private final PlaylistTrackRepository playlistTrackRepository;

    public TrackService(TrackRepository trackRepository, 
                        UserRepository userRepository,
                        TrackLikeRepository trackLikeRepository,
                        PlaylistTrackRepository playlistTrackRepository) throws IOException {
        this.trackRepository = trackRepository;
        this.userRepository = userRepository;
        this.trackLikeRepository = trackLikeRepository;
        this.playlistTrackRepository = playlistTrackRepository;
        Files.createDirectories(this.fileStorageLocation);
    }

    public Track uploadTrack(MultipartFile file, MultipartFile image, String title, String artistName, String userEmail) throws IOException {
        var fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        var targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        String imageFileName = null;
        if (image != null && !image.isEmpty()) {
            imageFileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            var imageTarget = this.fileStorageLocation.resolve(imageFileName);
            Files.copy(image.getInputStream(), imageTarget, StandardCopyOption.REPLACE_EXISTING);
        }

        String userId = "Anonymous";
        String artist = (artistName != null && !artistName.trim().isEmpty()) ? artistName : "Anonymous";

        if (userEmail != null) {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userId = user.getUuid().toString();
                if (artistName == null || artistName.trim().isEmpty()) {
                    artist = user.getUsername();
                }
            }
        }
        var newTrack = new Track(title, artist, userId, fileName, file.getContentType(), targetLocation.toString(), imageFileName);
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

    public List<Track> getTracksByUserId(String userId) {
        return trackRepository.findAll().stream()
                .filter(track -> userId.equals(track.getUserId()))
                .collect(java.util.stream.Collectors.toList());
    }

    public void deleteTrack(java.util.UUID trackId, String userEmail) throws IOException {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Track track = trackRepository.findById(trackId).orElseThrow(() -> new IllegalArgumentException("Track not found"));

        if (!track.getUserId().equals(user.getUuid().toString())) {
            throw new SecurityException("You are not authorized to delete this track");
        }

        Files.deleteIfExists(fileStorageLocation.resolve(track.getFilename()));
        if (track.getImageFilePath() != null) {
            Files.deleteIfExists(fileStorageLocation.resolve(track.getImageFilePath()));
        }
        trackRepository.delete(track);
    }

    public void deleteAllTracksByUser(String userEmail) throws IOException {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Track> tracks = getTracksByUserId(user.getUuid().toString());
        for (Track track : tracks) {
            deleteTrack(track.getId(), userEmail);
        }
    }

    public List<Track> getTracksByUserEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Collections.emptyList();
        }
        String userId = userOpt.get().getUuid().toString();
        return trackRepository.findAll().stream()
                .filter(track -> userId.equals(track.getUserId()))
                .collect(Collectors.toList());
    }

    public boolean addToPlaylist(java.util.UUID trackId, String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();

        Optional<PlaylistTrack> existing = playlistTrackRepository.findByUserIdAndTrackId(user.getUuid(), trackId);
        if (existing.isPresent()) return true; // Already in playlist

        playlistTrackRepository.save(new PlaylistTrack(user.getUuid(), trackId));
        return true;
    }

    public List<Track> getPlaylistTracks(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return Collections.emptyList();
        
        List<PlaylistTrack> playlistEntries = playlistTrackRepository.findByUserId(userOpt.get().getUuid());
        List<java.util.UUID> trackIds = playlistEntries.stream().map(PlaylistTrack::getTrackId).collect(Collectors.toList());
        
        return trackRepository.findAllById(trackIds);
    }

    public boolean isTrackInPlaylist(java.util.UUID trackId, String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();
        return playlistTrackRepository.findByUserIdAndTrackId(user.getUuid(), trackId).isPresent();
    }
}