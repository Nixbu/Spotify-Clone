package com.example.spotifyclone.controller;

import com.example.spotifyclone.entity.Album;
import com.example.spotifyclone.entity.Playlist;
import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.service.AlbumService;
import com.example.spotifyclone.service.PlaylistService;
import com.example.spotifyclone.service.SongService;
import com.example.spotifyclone.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/player")
public class PlayerController {

    private final SongService songService;
    private final PlaylistService playlistService;
    private final UserService userService;
    private final AlbumService albumService; // Added AlbumService dependency

    @Autowired
    public PlayerController(SongService songService, PlaylistService playlistService,
                            UserService userService, AlbumService albumService) { // Injected AlbumService
        this.songService = songService;
        this.playlistService = playlistService;
        this.userService = userService;
        this.albumService = albumService;
    }

    // This single, unified method now handles all player loading cases
    @GetMapping(value = {"", "/{id}"})
    public String playerPage(
            @PathVariable(required = false) Long id,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam(value = "playlistId", required = false) Long playlistId,
            Authentication authentication,
            Model model) {

        List<Song> songs;
        Optional<Song> currentSongOpt = (id != null) ? songService.findById(id) : Optional.empty();
        String pageTitle = "Music Player";
        String contextName = "All Songs";

        if (albumId != null) {
            // Case 1: Playing from an album
            songs = songService.findByAlbumId(albumId);
            Album album = albumService.findByIdWithSongs(albumId);
            if (album != null) {
                pageTitle = "Playing: " + album.getTitle();
                contextName = "Album: " + album.getTitle();
            }
            // If "Play Album" was clicked (no specific song id), set the first song as current
            if (currentSongOpt.isEmpty() && !songs.isEmpty()) {
                currentSongOpt = Optional.of(songs.get(0));
            }

        } else if (playlistId != null) {
            // Case 2: Playing from a playlist
            // (Existing logic for playlists remains)
            songs = new ArrayList<>(); // Initialize to avoid errors
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                Playlist playlist = playlistService.findByIdWithSongs(playlistId);
                if (playlist != null && playlist.getUser().getId().equals(userOpt.get().getId())) {
                    songs = new ArrayList<>(playlist.getSongs());
                    pageTitle = "Playing: " + playlist.getName();
                    contextName = "Playlist: " + playlist.getName();
                }
            } else {
                return "redirect:/login";
            }
        } else {
            // Case 3: Default - play all songs
            songs = songService.findAllSongsOrderByIdDesc();
        }

        model.addAttribute("songs", songs);
        model.addAttribute("currentSong", currentSongOpt.orElse(null));
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("contextName", contextName); // For display in the player queue header

        return "pages/player";
    }
}