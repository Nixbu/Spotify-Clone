package com.example.spotifyclone.controller;

import com.example.spotifyclone.entity.Album;
import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.service.AlbumService;
import com.example.spotifyclone.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final SongService songService;
    private final AlbumService albumService;

    @Autowired
    public HomeController(SongService songService, AlbumService albumService) {
        this.songService = songService;
        this.albumService = albumService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Get recent songs (limit to 6 for display)
        List<Song> recentSongs = songService.findAllSongsOrderByIdDesc();
        if (recentSongs.size() > 6) {
            recentSongs = recentSongs.subList(0, 6);
        }

        // Get featured albums (limit to 4 for display)
        List<Album> featuredAlbums = albumService.findAlbumsWithSongs();
        if (featuredAlbums.size() > 4) {
            featuredAlbums = featuredAlbums.subList(0, 4);
        }

        // Add statistics
        long totalSongs = songService.getTotalSongCount();
        long totalAlbums = albumService.getTotalAlbumCount();

        model.addAttribute("recentSongs", recentSongs);
        model.addAttribute("featuredAlbums", featuredAlbums);
        model.addAttribute("totalSongs", totalSongs);
        model.addAttribute("totalAlbums", totalAlbums);

        return "home";
    }
}