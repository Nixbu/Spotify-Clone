package com.example.spotifyclone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "songs")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Song title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Artist name is required")
    @Column(nullable = false)
    private String artist;

    private String genre;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    private Integer duration; // Duration in seconds

    @Column(name = "upload_date")
    private LocalDate uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @ManyToMany(mappedBy = "songs", fetch = FetchType.LAZY)
    private Set<Playlist> playlists = new HashSet<>();

    @ManyToMany(mappedBy = "favoriteSongs", fetch = FetchType.LAZY)
    private Set<User> favoriteByUsers = new HashSet<>();

    // Default constructor
    public Song() {}

    // Constructor
    public Song(String title, String artist, String genre, String filePath, User uploadedBy) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.filePath = filePath;
        this.uploadedBy = uploadedBy;
        this.uploadDate = LocalDate.now(); // Optional: default upload date at construction
    }

    @PrePersist
    protected void onCreate() {
        if (uploadDate == null) {
            uploadDate = LocalDate.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public LocalDate getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDate uploadDate) { this.uploadDate = uploadDate; }

    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public Set<Playlist> getPlaylists() { return playlists; }
    public void setPlaylists(Set<Playlist> playlists) { this.playlists = playlists; }

    public Set<User> getFavoriteByUsers() { return favoriteByUsers; }
    public void setFavoriteByUsers(Set<User> favoriteByUsers) { this.favoriteByUsers = favoriteByUsers; }

    // Helper methods
    public String getFormattedDuration() {
        if (duration == null) return "Unknown";
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getFileName() {
        if (filePath == null) return "";
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }
}
