package com.example.spotifyclone.controller;

import com.example.spotifyclone.entity.Playlist;
import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.entity.User;
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

    @Autowired
    public PlayerController(SongService songService, PlaylistService playlistService, UserService userService) {
        this.songService = songService;
        this.playlistService = playlistService;
        this.userService = userService;
    }

    @GetMapping
    public String playerPage(@RequestParam(value = "playlist", required = false) Long playlistId,
                             Authentication authentication,
                             Model model) {

        List<Song> songs;
        String pageTitle = "Music Player";

        if (playlistId != null) {
            // Playing from a playlist
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isEmpty()) {
                return "redirect:/login";
            }

            Playlist playlist = playlistService.findByIdWithSongs(playlistId);
            if (playlist == null || !playlist.getUser().getId().equals(userOpt.get().getId())) {
                // Playlist not found or not owned by user, fallback to all songs
                songs = songService.findAllSongsOrderByIdDesc();
            } else {
                songs = new ArrayList<>(playlist.getSongs());
                pageTitle = "Playing: " + playlist.getName();
                model.addAttribute("currentPlaylist", playlist);
            }
        } else {
            // Playing all songs
            songs = songService.findAllSongsOrderByIdDesc();
        }

        model.addAttribute("songs", songs);
        model.addAttribute("pageTitle", pageTitle);

        return "pages/player";
    }

    @GetMapping("/{id}")
    public String playerWithSong(@PathVariable Long id,
                                 @RequestParam(value = "playlist", required = false) Long playlistId,
                                 Authentication authentication,
                                 Model model) {

        Optional<Song> songOpt = songService.findById(id);
        if (songOpt.isEmpty()) {
            if (playlistId != null) {
                return "redirect:/player?playlist=" + playlistId;
            }
            return "redirect:/player";
        }

        Song currentSong = songOpt.get();
        List<Song> songs;
        String pageTitle = "Music Player";

        if (playlistId != null) {
            // Playing from a playlist
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isEmpty()) {
                return "redirect:/login";
            }

            Playlist playlist = playlistService.findByIdWithSongs(playlistId);
            if (playlist == null || !playlist.getUser().getId().equals(userOpt.get().getId())) {
                // Playlist not found or not owned by user, fallback to all songs
                songs = songService.findAllSongsOrderByIdDesc();
            } else {
                songs = new ArrayList<>(playlist.getSongs());
                pageTitle = "Playing: " + playlist.getName();
                model.addAttribute("currentPlaylist", playlist);
            }
        } else {
            // Playing all songs
            songs = songService.findAllSongsOrderByIdDesc();
        }

        model.addAttribute("songs", songs);
        model.addAttribute("currentSong", currentSong);
        model.addAttribute("pageTitle", pageTitle);

        return "pages/player";
    }
}