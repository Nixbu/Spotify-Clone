package com.example.spotifyclone.service;

import com.example.spotifyclone.entity.Album;
import com.example.spotifyclone.repository.AlbumRepository;
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
public class AlbumService {

    private final AlbumRepository albumRepository;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    @Autowired
    public AlbumService(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public Album saveAlbum(Album album) {
        return albumRepository.save(album);
    }

    public Optional<Album> findById(Long id) {
        return albumRepository.findById(id);
    }

    public Album findByIdWithSongs(Long id) {
        return albumRepository.findByIdWithSongs(id);
    }

    public List<Album> findAllAlbums() {
        return albumRepository.findAll();
    }

    public List<Album> findAllAlbumsOrderByReleaseDate() {
        return albumRepository.findAllOrderByReleaseDateDesc();
    }

    public List<Album> findAlbumsWithSongs() {
        return albumRepository.findAlbumsWithSongs();
    }

    public List<Album> searchAlbums(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllAlbums();
        }
        return albumRepository.searchAlbums(keyword.trim());
    }

    public List<Album> findByArtist(String artist) {
        return albumRepository.findByArtistContainingIgnoreCase(artist);
    }

    public List<Album> findByTitle(String title) {
        return albumRepository.findByTitleContainingIgnoreCase(title);
    }

    public Album updateAlbum(Album album) {
        return albumRepository.save(album);
    }

    public void deleteAlbum(Long id) {
        Optional<Album> albumOpt = albumRepository.findById(id);
        if (albumOpt.isPresent()) {
            Album album = albumOpt.get();
            // Delete cover image file if exists
            if (album.getCoverImagePath() != null) {
                try {
                    Path imagePath = Paths.get(album.getCoverImagePath());
                    Files.deleteIfExists(imagePath);
                } catch (IOException e) {
                    System.err.println("Failed to delete cover image: " + album.getCoverImagePath());
                }
            }
            albumRepository.deleteById(id);
        }
    }

    public String uploadCoverImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Only image files are allowed");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, "covers");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public boolean isValidImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    public long getTotalAlbumCount() {
        return albumRepository.count();
    }
}