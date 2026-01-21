package com.mossaq.mossaq.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    private UUID id;
    private String title;
    private String artist;
    private String filename;
    private String contentType;
    private String audioFilePath;
    private String imageFilePath;
    private int playCount;
    private int likeCount;


    public Track() {
    }

    public Track(String title, String artist, String filename, String contentType) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.artist = artist;
        this.filename = filename;
        this.contentType = contentType;
        this.playCount = 0;
        this.likeCount = 0;
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

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

}