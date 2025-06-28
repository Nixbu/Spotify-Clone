package com.example.spotifyclone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
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

    private String genre;

    @Min(value = 1900, message = "Release year must be at least 1900")
    @Max(value = 2030, message = "Release year cannot be later than 2030")
    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "cover_image_path")
    private String coverImagePath;

    // CascadeType.ALL and orphanRemoval=true to ensure songs are deleted with the album.
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Song> songs = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy;

    // Default constructor
    public Album() {}

    // Updated constructor
    public Album(String title, String artist, String genre, Integer releaseYear, String description, String coverImagePath) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.description = description;
        this.coverImagePath = coverImagePath;
        if (releaseYear != null) {
            this.releaseDate = LocalDate.of(releaseYear, 1, 1);
        }
    }

    // Constructor with uploadedBy
    public Album(String title, String artist, String genre, Integer releaseYear, String description, String coverImagePath, User uploadedBy) {
        this(title, artist, genre, releaseYear, description, coverImagePath);
        this.uploadedBy = uploadedBy;
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

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
        if (releaseYear != null) {
            this.releaseDate = LocalDate.of(releaseYear, 1, 1);
        }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public Set<Song> getSongs() { return songs; }
    public void setSongs(Set<Song> songs) { this.songs = songs; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

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

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", genre='" + genre + '\'' +
                ", releaseYear=" + releaseYear +
                ", description='" + description + '\'' +
                ", coverImagePath='" + coverImagePath + '\'' +
                ", uploadedBy=" + (uploadedBy != null ? uploadedBy.getUsername() : "null") +
                '}';
    }
}
