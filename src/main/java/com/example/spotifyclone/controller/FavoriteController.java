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
}