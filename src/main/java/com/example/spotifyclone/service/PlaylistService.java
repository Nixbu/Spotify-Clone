package com.example.spotifyclone.service;

import com.example.spotifyclone.entity.Playlist;
import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    @Autowired
    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    public Playlist savePlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    public Optional<Playlist> findById(Long id) {
        return playlistRepository.findById(id);
    }

    public Playlist findByIdWithSongs(Long id) {
        return playlistRepository.findByIdWithSongs(id);
    }

    public List<Playlist> findAllPlaylists() {
        return playlistRepository.findAll();
    }

    public List<Playlist> findByUser(User user) {
        return playlistRepository.findByUser(user);
    }

    public List<Playlist> findByUserOrderByCreatedAt(User user) {
        return playlistRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Playlist> findByUserId(Long userId) {
        return playlistRepository.findByUserId(userId);
    }

    public List<Playlist> findByUserIdAndNameContaining(Long userId, String name) {
        return playlistRepository.findByUserIdAndNameContaining(userId, name);
    }

    public long countByUserId(Long userId) {
        return playlistRepository.countByUserId(userId);
    }

    public Playlist updatePlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    public void deletePlaylist(Long id) {
        playlistRepository.deleteById(id);
    }

    public void addSongToPlaylist(Long playlistId, Song song) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();
            playlist.addSong(song);
            playlistRepository.save(playlist);
        }
    }

    public void removeSongFromPlaylist(Long playlistId, Song song) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();
            playlist.removeSong(song);
            playlistRepository.save(playlist);
        }
    }

    public boolean isPlaylistOwner(Long playlistId, Long userId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        return playlistOpt.isPresent() && playlistOpt.get().getUser().getId().equals(userId);
    }

    public boolean playlistContainsSong(Long playlistId, Long songId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isPresent()) {
            return playlistOpt.get().getSongs().stream()
                    .anyMatch(song -> song.getId().equals(songId));
        }
        return false;
    }

    public Playlist createPlaylist(String name, String description, User user) {
        Playlist playlist = new Playlist(name, description, user);
        return savePlaylist(playlist);
    }
}