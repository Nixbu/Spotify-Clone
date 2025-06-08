package com.example.spotifyclone.controller;

import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.service.SongService;
import com.example.spotifyclone.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    private final UserService userService;
    private final SongService songService;

    @Autowired
    public FavoriteController(UserService userService, SongService songService) {
        this.userService = userService;
        this.songService = songService;
    }

    @GetMapping
    public String favoritesPage(Authentication authentication, Model model) {
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        List<Song> favoriteSongs = songService.findFavoritesByUserId(user.getId());

        model.addAttribute("songs", favoriteSongs);
        model.addAttribute("favoriteCount", favoriteSongs.size());
        return "pages/favorites";
    }

    @PostMapping("/add")
    @ResponseBody
    public String addToFavorites(@RequestParam Long songId,
                                 Authentication authentication) {
        try {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            Optional<Song> songOpt = songService.findById(songId);

            if (userOpt.isPresent() && songOpt.isPresent()) {
                userService.addFavoriteSong(userOpt.get(), songOpt.get());
                return "success";
            }
            return "error";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public String removeFromFavorites(@RequestParam Long songId,
                                      Authentication authentication) {
        try {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            Optional<Song> songOpt = songService.findById(songId);

            if (userOpt.isPresent() && songOpt.isPresent()) {
                userService.removeFavoriteSong(userOpt.get(), songOpt.get());
                return "success";
            }
            return "error";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/{id}/remove")
    public String removeFavorite(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        Optional<Song> songOpt = songService.findById(id);

        if (userOpt.isPresent() && songOpt.isPresent()) {
            userService.removeFavoriteSong(userOpt.get(), songOpt.get());
            redirectAttributes.addFlashAttribute("successMsg", "Song removed from favorites!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to remove song from favorites!");
        }

        return "redirect:/favorites";
    }

    @GetMapping("/check/{songId}")
    @ResponseBody
    public boolean isFavorite(@PathVariable Long songId,
                              Authentication authentication) {
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isPresent()) {
            User user = userService.findByIdWithFavorites(userOpt.get().getId()).orElse(null);
            if (user != null) {
                return user.getFavoriteSongs().stream()
                        .anyMatch(song -> song.getId().equals(songId));
            }
        }
        return false;
    }
}