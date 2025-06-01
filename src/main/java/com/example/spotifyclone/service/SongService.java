package com.example.spotifyclone.service;

import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SongService {

    private final SongRepository songRepository;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    @Autowired
    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public Song saveSong(Song song) {
        return songRepository.save(song);
    }

    public Optional<Song> findById(Long id) {
        return songRepository.findById(id);
    }

    public List<Song> findAllSongs() {
        return songRepository.findAll();
    }

    public List<Song> findAllSongsOrderByIdDesc() {
        return songRepository.findAllOrderByIdDesc();
    }

    public List<Song> findByUploadedBy(User user) {
        return songRepository.findByUploadedBy(user);
    }

    public List<Song> searchSongs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllSongs();
        }
        return songRepository.searchSongs(keyword.trim());
    }

    public List<Song> findByArtist(String artist) {
        return songRepository.findByArtistContainingIgnoreCase(artist);
    }

    public List<Song> findByTitle(String title) {
        return songRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Song> findByGenre(String genre) {
        return songRepository.findByGenreContainingIgnoreCase(genre);
    }

    public List<Song> findByAlbumId(Long albumId) {
        return songRepository.findByAlbumId(albumId);
    }

    public List<Song> findFavoritesByUserId(Long userId) {
        return songRepository.findFavoritesByUserId(userId);
    }

    public Song updateSong(Song song) {
        return songRepository.save(song);
    }

    public void deleteSong(Long id) {
        Optional<Song> songOpt = songRepository.findById(id);
        if (songOpt.isPresent()) {
            Song song = songOpt.get();
            // Delete the file from filesystem
            try {
                Path filePath = Paths.get(song.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log error but continue with database deletion
                System.err.println("Failed to delete file: " + song.getFilePath());
            }
            songRepository.deleteById(id);
        }
    }

    public String uploadSongFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("audio/mpeg")) {
            throw new IOException("Only MP3 files are allowed");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".mp3";
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public boolean isValidMp3File(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        return (contentType != null && contentType.equals("audio/mpeg")) ||
                (originalFilename != null && originalFilename.toLowerCase().endsWith(".mp3"));
    }

    public long getTotalSongCount() {
        return songRepository.count();
    }
}