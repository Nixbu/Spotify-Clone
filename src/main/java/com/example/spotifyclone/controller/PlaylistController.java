package com.example.spotifyclone.controller;

import com.example.spotifyclone.entity.Playlist;
import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.service.PlaylistService;
import com.example.spotifyclone.service.SongService;
import com.example.spotifyclone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final UserService userService;
    private final SongService songService;

    @Autowired
    public PlaylistController(PlaylistService playlistService, UserService userService, SongService songService) {
        this.playlistService = playlistService;
        this.userService = userService;
        this.songService = songService;
    }

    @GetMapping
    public String playlistsPage(@RequestParam(value = "search", required = false) String search,
                                Authentication authentication,
                                Model model) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        List<Playlist> playlists;
        if (search != null && !search.trim().isEmpty()) {
            playlists = playlistService.findByUserIdAndNameContaining(user.getId(), search);
            model.addAttribute("searchTerm", search);
        } else {
            playlists = playlistService.findByUserOrderByCreatedAt(user);
        }
        model.addAttribute("playlists", playlists);
        model.addAttribute("playlist", new Playlist());
        model.addAttribute("playlistCount", playlistService.countByUserId(user.getId()));
        return "pages/playlists";
    }

    @PostMapping("/create")
    public String createPlaylist(@Valid @ModelAttribute("playlist") Playlist playlist,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Playlist name is required.");
            return "redirect:/playlists";
        }

        playlistService.createPlaylist(playlist.getName(), playlist.getDescription(), user);
        redirectAttributes.addFlashAttribute("successMsg", "Playlist created successfully!");
        return "redirect:/playlists";
    }

    @GetMapping("/{id}")
    public String playlistDetails(@PathVariable Long id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        if (!playlistService.isUserMember(id, user.getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "You do not have access to this playlist.");
            return "redirect:/playlists";
        }

        Playlist playlist = playlistService.findByIdWithSongsAndUsers(id);
        model.addAttribute("playlist", playlist);
        model.addAttribute("allSongs", songService.findAllSongs());
        return "pages/playlist-details";
    }

    @PostMapping("/{id}/add-song")
    public String addSongToPlaylist(@PathVariable Long id, @RequestParam Long songId, Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        if (!playlistService.isUserMember(id, user.getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "You can only modify playlists you are a member of.");
            return "redirect:/playlists/" + id;
        }

        if (playlistService.playlistContainsSong(id, songId)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Song is already in this playlist.");
            return "redirect:/playlists/" + id;
        }

        songService.findById(songId).ifPresentOrElse(
                song -> {
                    playlistService.addSongToPlaylist(id, song);
                    redirectAttributes.addFlashAttribute("successMsg", "Song added successfully!");
                },
                () -> redirectAttributes.addFlashAttribute("errorMsg", "Song not found.")
        );
        return "redirect:/playlists/" + id;
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Void> removeSongFromPlaylist(@PathVariable Long playlistId, @PathVariable Long songId, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElse(null);
        if (user == null || !playlistService.isUserMember(playlistId, user.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        songService.findById(songId).ifPresent(song -> playlistService.removeSongFromPlaylist(playlistId, song));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElse(null);
        if (user == null || !playlistService.isUserMember(id, user.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        playlistService.leaveOrDeletePlaylist(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/update")
    public String updatePlaylist(@PathVariable Long id, @RequestParam String name, @RequestParam(required = false) String description, Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        if (!playlistService.isUserMember(id, user.getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Access denied.");
            return "redirect:/playlists";
        }

        Playlist playlist = playlistService.findById(id).orElseThrow();
        playlist.setName(name);
        playlist.setDescription(description);
        playlistService.updatePlaylist(playlist);

        redirectAttributes.addFlashAttribute("successMsg", "Playlist updated successfully!");
        return "redirect:/playlists/" + id;
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<String> sharePlaylist(@PathVariable Long id, @RequestParam String username, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName()).orElse(null);
        if (currentUser == null) return ResponseEntity.status(401).body("Unauthorized");

        if (!playlistService.isUserMember(id, currentUser.getId())) {
            return ResponseEntity.status(403).body("You can only share playlists you are a member of.");
        }

        Optional<User> userToShareWithOpt = userService.findByUsername(username);
        if (userToShareWithOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }
        User userToShareWith = userToShareWithOpt.get();

        if (playlistService.isUserMember(id, userToShareWith.getId())) {
            return ResponseEntity.badRequest().body("User already has access to this playlist.");
        }

        playlistService.sharePlaylistWithUser(id, userToShareWith.getId(), currentUser.getId());
        return ResponseEntity.ok("Playlist shared successfully with " + username);
    }
}