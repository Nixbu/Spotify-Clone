package com.example.spotifyclone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Album title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Artist name is required")
    @Column(nullable = false)
    private String artist;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "cover_image_path")
    private String coverImagePath;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Song> songs = new HashSet<>();

    // Default constructor
    public Album() {}

    // Constructor
    public Album(String title, String artist, LocalDate releaseDate, String coverImagePath) {
        this.title = title;
        this.artist = artist;
        this.releaseDate = releaseDate;
        this.coverImagePath = coverImagePath;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public Set<Song> getSongs() { return songs; }
    public void setSongs(Set<Song> songs) { this.songs = songs; }

    // Helper methods
    public void addSong(Song song) {
        this.songs.add(song);
        song.setAlbum(this);
    }

    public void removeSong(Song song) {
        this.songs.remove(song);
        song.setAlbum(null);
    }

    public int getSongCount() {
        return songs.size();
    }
}