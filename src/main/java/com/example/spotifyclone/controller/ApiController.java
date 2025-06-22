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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final SongService songService;
    private final AlbumService albumService;
    private final PlaylistService playlistService;
    private final UserService userService;

    @Autowired
    public ApiController(SongService songService, AlbumService albumService,
                         PlaylistService playlistService, UserService userService) {
        this.songService = songService;
        this.albumService = albumService;
        this.playlistService = playlistService;
        this.userService = userService;
    }

    @GetMapping("/songs")
    public ResponseEntity<List<Song>> getAllSongs() {
        List<Song> songs = songService.findAllSongsOrderByIdDesc();
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/songs/{id}")
    public ResponseEntity<Song> getSong(@PathVariable Long id) {
        Optional<Song> songOpt = songService.findById(id);
        return songOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/songs/search")
    public ResponseEntity<List<Song>> searchSongs(@RequestParam String q) {
        List<Song> songs = songService.searchSongs(q);
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/albums")
    public ResponseEntity<List<Album>> getAllAlbums() {
        List<Album> albums = albumService.findAllAlbums();
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/albums/{id}")
    public ResponseEntity<Album> getAlbum(@PathVariable Long id) {
        Album album = albumService.findByIdWithSongs(id);
        return album != null ? ResponseEntity.ok(album)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/albums/{id}/songs")
    public ResponseEntity<List<Song>> getAlbumSongs(@PathVariable Long id) {
        List<Song> songs = songService.findByAlbumId(id);
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/playlists")
    public ResponseEntity<List<Playlist>> getUserPlaylists(Authentication authentication) {
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        List<Playlist> playlists = playlistService.findByUser(userOpt.get());
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/playlists/{id}")
    public ResponseEntity<Playlist> getPlaylist(@PathVariable Long id,
                                                Authentication authentication) {
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        Playlist playlist = playlistService.findByIdWithSongs(id);
        if (playlist == null || !playlist.getUser().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(playlist);
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<Song>> getFavorites(Authentication authentication) {
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        List<Song> favorites = songService.findFavoritesByUserId(userOpt.get().getId());
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/favorites/{songId}")
    public ResponseEntity<String> addToFavorites(@PathVariable Long songId,
                                                 Authentication authentication) {
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        Optional<Song> songOpt = songService.findById(songId);

        if (userOpt.isPresent() && songOpt.isPresent()) {
            User user = userOpt.get();
            Song song = songOpt.get();

            // Check if song is already in favorites
            if (userService.isSongInFavorites(user, song)) {
                return ResponseEntity.ok("Song already in favorites");
            }

            try {
                userService.addFavoriteSong(user, song);
                return ResponseEntity.ok("Added to favorites");
            } catch (Exception e) {
                // Handle any database constraint violations or other exceptions
                return ResponseEntity.ok("Song already in favorites");
            }
        }

        return ResponseEntity.badRequest().body("Failed to add to favorites");
    }

    @DeleteMapping("/favorites/{songId}")
    public ResponseEntity<String> removeFromFavorites(@PathVariable Long songId,
                                                      Authentication authentication) {
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        Optional<Song> songOpt = songService.findById(songId);

        if (userOpt.isPresent() && songOpt.isPresent()) {
            userService.removeFavoriteSong(userOpt.get(), songOpt.get());
            return ResponseEntity.ok("Removed from favorites");
        }

        return ResponseEntity.badRequest().body("Failed to remove from favorites");
    }
}