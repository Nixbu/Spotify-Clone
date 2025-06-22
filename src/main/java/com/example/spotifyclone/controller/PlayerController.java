package com.example.spotifyclone.controller;

import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/player")
public class PlayerController {

    private final SongService songService;

    @Autowired
    public PlayerController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping
    public String playerPage(Model model) {
        List<Song> allSongs = songService.findAllSongsOrderByIdDesc();
        model.addAttribute("songs", allSongs);
        return "pages/player";
    }

    @GetMapping("/{id}")
    public String playerWithSong(@PathVariable Long id, Model model) {
        Optional<Song> songOpt = songService.findById(id);
        if (songOpt.isEmpty()) {
            return "redirect:/player";
        }

        Song currentSong = songOpt.get();
        List<Song> allSongs = songService.findAllSongsOrderByIdDesc();

        model.addAttribute("songs", allSongs);
        model.addAttribute("currentSong", currentSong);

        return "pages/player";
    }
}