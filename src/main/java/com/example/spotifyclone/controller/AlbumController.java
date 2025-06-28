package com.example.spotifyclone.controller;

import com.example.spotifyclone.entity.Album;
import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.service.AlbumService;
import com.example.spotifyclone.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/albums")
public class AlbumController {

    private final AlbumService albumService;
    private final SongService songService;
    private final String coverUploadDir = "uploads/covers/";

    @Autowired
    public AlbumController(AlbumService albumService, SongService songService) {
        this.albumService = albumService;
        this.songService = songService;
    }

    @GetMapping
    public String listAlbums(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Album> albums;
        if (keyword != null && !keyword.isEmpty()) {
            albums = albumService.searchAlbums(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            albums = albumService.findAllAlbums();
        }
        model.addAttribute("albums", albums);
        return "pages/albums";
    }

    @GetMapping("/upload")
    public String showUploadAlbumForm(Model model) {
        model.addAttribute("album", new Album());
        return "pages/upload-album";
    }

    @PostMapping("/upload")
    public String uploadAlbum(@Valid @ModelAttribute("album") Album album,
                              BindingResult bindingResult,
                              @RequestParam("coverImageFile") MultipartFile coverImageFile,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        if (bindingResult.hasErrors()) {
            return "pages/upload-album";
        }

        if (!coverImageFile.isEmpty()) {
            if (!coverImageFile.getContentType().startsWith("image/")) {
                model.addAttribute("fileError", "Please select a valid image file (JPEG, PNG, GIF).");
                return "pages/upload-album";
            }
            try {
                Path coverUploadPath = Paths.get(coverUploadDir);
                if (!Files.exists(coverUploadPath)) {
                    Files.createDirectories(coverUploadPath);
                }
                String originalFilename = coverImageFile.getOriginalFilename();
                String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
                Path filePath = coverUploadPath.resolve(uniqueFilename);
                Files.copy(coverImageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                album.setCoverImagePath("/uploads/covers/" + uniqueFilename);
            } catch (IOException e) {
                model.addAttribute("errorMsg", "Failed to upload cover image: " + e.getMessage());
                return "pages/upload-album";
            }
        }

        albumService.saveAlbum(album);
        redirectAttributes.addFlashAttribute("successMsg", "Album created successfully!");
        return "redirect:/albums";
    }

    @GetMapping("/{id}")
    public String albumDetails(@PathVariable Long id, Model model) {
        Optional<Album> albumOptional = Optional.ofNullable(albumService.findByIdWithSongs(id));
        if (albumOptional.isPresent()) {
            Album album = albumOptional.get();
            List<Song> songs = songService.findByAlbumId(id);
            model.addAttribute("album", album);
            model.addAttribute("songs", songs);
            return "pages/album-details";
        }
        return "redirect:/albums?error=notfound";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAlbum(@PathVariable Long id) {
        try {
            albumService.deleteAlbum(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting album: " + e.getMessage());
        }
    }
}
