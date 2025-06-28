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
    private final AlbumService albumService;

    @Autowired
    public PlayerController(SongService songService, PlaylistService playlistService,
                            UserService userService, AlbumService albumService) {
        this.songService = songService;
        this.playlistService = playlistService;
        this.userService = userService;
        this.albumService = albumService;
    }

    @GetMapping(value = {"", "/{id}"})
    public String playerPage(
            @PathVariable(required = false) Long id,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam(value = "playlistId", required = false) Long playlistId,
            @RequestParam(value = "playFavorites", required = false) boolean playFavorites,
            Authentication authentication,
            Model model) {

        List<Song> songs = new ArrayList<>();
        Optional<Song> currentSongOpt = (id != null) ? songService.findById(id) : Optional.empty();
        String pageTitle = "Music Player";
        String contextName = "All Songs";
        Optional<User> userOpt = userService.findByUsername(authentication.getName());

        if (userOpt.isEmpty()) {
            return "redirect:/login"; // User must be logged in for most actions
        }
        User currentUser = userOpt.get();


        if (albumId != null) {
            // Case 1: Playing from an album
            songs = songService.findByAlbumId(albumId);
            Album album = albumService.findByIdWithSongs(albumId);
            if (album != null) {
                pageTitle = "Playing: " + album.getTitle();
                contextName = "Album: " + album.getTitle();
            }
        } else if (playFavorites) {
            // Case 2: Play all favorite songs
            songs = songService.findFavoritesByUserId(currentUser.getId());
            pageTitle = "Playing: Liked Songs";
            contextName = "Your Liked Songs";
        } else if (playlistId != null) {
            // Case 3: Playing from a playlist - CORRECTED LOGIC
            // Ensure the user is a member of the playlist
            if (playlistService.isUserMember(playlistId, currentUser.getId())) {
                Playlist playlist = playlistService.findByIdWithSongsAndUsers(playlistId);
                if (playlist != null) {
                    songs = new ArrayList<>(playlist.getSongs());
                    pageTitle = "Playing: " + playlist.getName();
                    contextName = "Playlist: " + playlist.getName();
                }
            }
        } else {
            // Case 4: Default - play all songs (or handle as an error if not desired)
            songs = songService.findAllSongsOrderByIdDesc();
        }

        // If a playlist/album/favorites was requested without a specific song, play the first one
        if (currentSongOpt.isEmpty() && !songs.isEmpty()) {
            currentSongOpt = Optional.of(songs.get(0));
        }


        model.addAttribute("songs", songs);
        model.addAttribute("currentSong", currentSongOpt.orElse(null));
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("contextName", contextName); // For display in the player queue header

        return "pages/player";
    }
}