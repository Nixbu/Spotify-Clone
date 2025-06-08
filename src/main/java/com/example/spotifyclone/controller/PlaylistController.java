package com.example.spotifyclone.controller;

import com.example.spotifyclone.entity.Playlist;
import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.service.PlaylistService;
import com.example.spotifyclone.service.SongService;
import com.example.spotifyclone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
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
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                model.addAttribute("playlists", playlistService.findByUser(userOpt.get()));
            }
            return "pages/playlists";
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        playlist.setUser(userOpt.get());
        playlistService.savePlaylist(playlist);

        redirectAttributes.addFlashAttribute("successMsg", "Playlist created successfully!");
        return "redirect:/playlists";
    }

    @GetMapping("/{id}")
    public String playlistDetails(@PathVariable Long id,
                                  Authentication authentication,
                                  Model model) {

        Playlist playlist = playlistService.findByIdWithSongs(id);
        if (playlist == null) {
            return "redirect:/playlists";
        }

        // Check if user owns the playlist
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty() || !playlist.getUser().getId().equals(userOpt.get().getId())) {
            return "redirect:/playlists";
        }

        model.addAttribute("playlist", playlist);
        model.addAttribute("allSongs", songService.findAllSongs());
        return "pages/playlist-details";
    }

    @PostMapping("/{id}/add-song")
    public String addSongToPlaylist(@PathVariable Long id,
                                    @RequestParam Long songId,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        if (!playlistService.isPlaylistOwner(id, userOpt.get().getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "You can only modify your own playlists");
            return "redirect:/playlists/" + id;
        }

        if (playlistService.playlistContainsSong(id, songId)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Song is already in this playlist");
            return "redirect:/playlists/" + id;
        }

        Optional<Song> songOpt = songService.findById(songId);
        if (songOpt.isPresent()) {
            playlistService.addSongToPlaylist(id, songOpt.get());
            redirectAttributes.addFlashAttribute("successMsg", "Song added to playlist successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Song not found");
        }

        return "redirect:/playlists/" + id;
    }

    @PostMapping("/{id}/remove-song")
    public String removeSongFromPlaylist(@PathVariable Long id,
                                         @RequestParam Long songId,
                                         Authentication authentication,
                                         RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        if (!playlistService.isPlaylistOwner(id, userOpt.get().getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "You can only modify your own playlists");
            return "redirect:/playlists/" + id;
        }

        Optional<Song> songOpt = songService.findById(songId);
        if (songOpt.isPresent()) {
            playlistService.removeSongFromPlaylist(id, songOpt.get());
            redirectAttributes.addFlashAttribute("successMsg", "Song removed from playlist successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Song not found");
        }

        return "redirect:/playlists/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deletePlaylist(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        if (!playlistService.isPlaylistOwner(id, userOpt.get().getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "You can only delete your own playlists");
            return "redirect:/playlists";
        }

        playlistService.deletePlaylist(id);
        redirectAttributes.addFlashAttribute("successMsg", "Playlist deleted successfully!");
        return "redirect:/playlists";
    }

    @PostMapping("/{id}/update")
    public String updatePlaylist(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Optional<Playlist> playlistOpt = playlistService.findById(id);
        if (playlistOpt.isEmpty() || !playlistOpt.get().getUser().getId().equals(userOpt.get().getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Playlist not found or access denied");
            return "redirect:/playlists";
        }

        Playlist playlist = playlistOpt.get();
        playlist.setName(name);
        playlist.setDescription(description);
        playlistService.updatePlaylist(playlist);

        redirectAttributes.addFlashAttribute("successMsg", "Playlist updated successfully!");
        return "redirect:/playlists/" + id;
    }
}