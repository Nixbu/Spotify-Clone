package com.example.spotifyclone.controller;

import com.example.spotifyclone.entity.Album;
import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.service.AlbumService;
import com.example.spotifyclone.service.SongService;
import com.example.spotifyclone.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/songs")
public class SongController {

    private final SongService songService;
    private final UserService userService;
    private final AlbumService albumService;

    @Autowired
    public SongController(SongService songService, UserService userService, AlbumService albumService) {
        this.songService = songService;
        this.userService = userService;
        this.albumService = albumService;
    }

    @GetMapping
    public String songsPage(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "genre", required = false) String genre,
                            Model model) {

        List<Song> songs;
        if (search != null && !search.trim().isEmpty()) {
            songs = songService.searchSongs(search);
            model.addAttribute("searchTerm", search);
        } else if (genre != null && !genre.trim().isEmpty()) {
            songs = songService.findByGenre(genre);
            model.addAttribute("selectedGenre", genre);
        } else {
            songs = songService.findAllSongsOrderByIdDesc();
        }

        model.addAttribute("songs", songs);
        model.addAttribute("song", new Song()); // For upload form
        model.addAttribute("albums", albumService.findAllAlbums());
        return "pages/songs";
    }

    @PostMapping("/upload")
    public String uploadSong(@Valid @ModelAttribute("song") Song song,
                             BindingResult bindingResult,
                             @RequestParam("file") MultipartFile file,
                             @RequestParam(value = "albumId", required = false) Long albumId,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        // Validate form data
        if (bindingResult.hasErrors()) {
            model.addAttribute("songs", songService.findAllSongs());
            model.addAttribute("albums", albumService.findAllAlbums());
            return "pages/songs";
        }

        // Validate file
        if (file.isEmpty()) {
            model.addAttribute("errorMsg", "Please select an MP3 file to upload");
            model.addAttribute("songs", songService.findAllSongs());
            model.addAttribute("albums", albumService.findAllAlbums());
            return "pages/songs";
        }

        if (!songService.isValidMp3File(file)) {
            model.addAttribute("errorMsg", "Only MP3 files are allowed");
            model.addAttribute("songs", songService.findAllSongs());
            model.addAttribute("albums", albumService.findAllAlbums());
            return "pages/songs";
        }

        try {
            // Get current user
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isEmpty()) {
                model.addAttribute("errorMsg", "User not found");
                return "pages/songs";
            }

            User user = userOpt.get();

            // Upload file
            String filePath = songService.uploadSongFile(file);
            song.setFilePath(filePath);
            song.setUploadedBy(user);

            // Set album if selected
            if (albumId != null) {
                Optional<Album> albumOpt = albumService.findById(albumId);
                if (albumOpt.isPresent()) {
                    song.setAlbum(albumOpt.get());
                }
            }

            // Save song
            songService.saveSong(song);

            redirectAttributes.addFlashAttribute("successMsg", "Song uploaded successfully!");
            return "redirect:/songs";

        } catch (IOException e) {
            model.addAttribute("errorMsg", "Failed to upload file: " + e.getMessage());
            model.addAttribute("songs", songService.findAllSongs());
            model.addAttribute("albums", albumService.findAllAlbums());
            return "pages/songs";
        }
    }


    @GetMapping("/{id}")
    public String songDetails(@PathVariable Long id, Model model) {
        Optional<Song> songOpt = songService.findById(id);
        if (songOpt.isEmpty()) {
            return "redirect:/songs";
        }

        Song song = songOpt.get();
        model.addAttribute("song", song);

        // Get related songs (same artist or album)
        List<Song> relatedSongs;
        if (song.getAlbum() != null) {
            relatedSongs = songService.findByAlbumId(song.getAlbum().getId());
            relatedSongs.removeIf(s -> s.getId().equals(id)); // Remove current song
        } else {
            relatedSongs = songService.findByArtist(song.getArtist());
            relatedSongs.removeIf(s -> s.getId().equals(id)); // Remove current song
        }

        model.addAttribute("relatedSongs", relatedSongs);
        return "pages/song-details";
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamSong(@PathVariable Long id) {
        Optional<Song> songOpt = songService.findById(id);
        if (songOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Song song = songOpt.get();
        Path filePath = Paths.get(song.getFilePath());

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + song.getFileName() + "\"")
                .body(resource);
    }

    @PostMapping("/{id}/delete")
    public String deleteSong(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        Optional<Song> songOpt = songService.findById(id);
        if (songOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Song not found");
            return "redirect:/songs";
        }

        Song song = songOpt.get();

        // Check if user owns the song
        if (!song.getUploadedBy().getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("errorMsg", "You can only delete your own songs");
            return "redirect:/songs";
        }

        try {
            songService.deleteSong(id);
            redirectAttributes.addFlashAttribute("successMsg", "Song deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to delete song");
        }

        return "redirect:/songs";
    }

    @GetMapping("/my")
    public String mySongs(Authentication authentication, Model model) {
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/songs";
        }

        List<Song> mySongs = songService.findByUploadedBy(userOpt.get());
        model.addAttribute("songs", mySongs);
        model.addAttribute("pageTitle", "My Uploaded Songs");
        return "pages/my-songs";
    }

    // AJAX endpoint for quick search
    @GetMapping("/search")
    @ResponseBody
    public List<Song> searchSongs(@RequestParam String q) {
        return songService.searchSongs(q);
    }
}