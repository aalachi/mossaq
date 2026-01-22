package com.mossaq.mossaq.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    private UUID id;
    private String title;
    private String artist;
    private String userId;
    private String filename;
    private String contentType;
    private String audioFilePath;
    private String imageFilePath;
    private Integer playCount = 0;
    private Integer likeCount = 0;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "trackId", insertable = false, updatable = false)
    private List<Comment> comments = new ArrayList<>();


    public Track() {
    }

    public Track(String title, String artist, String userId, String filename, String contentType, String audioFilePath, String imageFilePath) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.artist = artist;
        this.userId = userId;
        this.filename = filename;
        this.contentType = contentType;
        this.audioFilePath = audioFilePath;
        this.imageFilePath = imageFilePath;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }

}