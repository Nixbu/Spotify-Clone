package com.example.spotifyclone.service;

import com.example.spotifyclone.entity.Playlist;
import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.repository.PlaylistRepository;
import com.example.spotifyclone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    @Autowired
    public PlaylistService(PlaylistRepository playlistRepository, UserRepository userRepository) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
    }

    public Optional<Playlist> findById(Long id) {
        return playlistRepository.findById(id);
    }

    public Playlist findByIdWithSongsAndUsers(Long id) {
        return playlistRepository.findByIdWithSongsAndUsers(id);
    }

    public List<Playlist> findByUser(User user) {
        return playlistRepository.findByUsersContaining(user);
    }

    public List<Playlist> findByUserOrderByCreatedAt(User user) {
        return playlistRepository.findByUsersContainingOrderByCreatedAtDesc(user);
    }

    public List<Playlist> findByUserIdAndNameContaining(Long userId, String name) {
        return playlistRepository.findByUserIdAndNameContaining(userId, name);
    }

    public long countByUserId(Long userId) {
        return playlistRepository.countByUserId(userId);
    }

    public Playlist createPlaylist(String name, String description, User user) {
        // Create playlist with user_id properly set
        Playlist playlist = new Playlist(name, description, user);
        return playlistRepository.save(playlist);
    }

    public Playlist updatePlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    public void leaveOrDeletePlaylist(Long playlistId, User user) {
        Playlist playlist = findByIdWithSongsAndUsers(playlistId);
        if (playlist != null && playlist.getUsers().contains(user)) {
            if (playlist.getUsers().size() > 1) {
                playlist.removeUser(user);
                playlistRepository.save(playlist);
            } else {
                playlistRepository.delete(playlist);
            }
        }
    }

    public void addSongToPlaylist(Long playlistId, Song song) {
        playlistRepository.findById(playlistId).ifPresent(playlist -> {
            playlist.addSong(song);
            playlistRepository.save(playlist);
        });
    }

    public void removeSongFromPlaylist(Long playlistId, Song song) {
        playlistRepository.findById(playlistId).ifPresent(playlist -> {
            playlist.removeSong(song);
            playlistRepository.save(playlist);
        });
    }

    public boolean isUserMember(Long playlistId, Long userId) {
        return playlistRepository.findById(playlistId)
                .map(playlist -> playlist.getUsers().stream().anyMatch(user -> user.getId().equals(userId)))
                .orElse(false);
    }

    public boolean playlistContainsSong(Long playlistId, Long songId) {
        return playlistRepository.findById(playlistId)
                .map(playlist -> playlist.getSongs().stream().anyMatch(song -> song.getId().equals(songId)))
                .orElse(false);
    }

    public boolean sharePlaylistWithUser(Long playlistId, Long userIdToAdd, Long currentUserId) {
        if (!isUserMember(playlistId, currentUserId)) {
            return false; // Only members can share
        }

        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        Optional<User> userToAddOpt = userRepository.findById(userIdToAdd);

        if (playlistOpt.isPresent() && userToAddOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();
            User userToAdd = userToAddOpt.get();
            if (!playlist.getUsers().contains(userToAdd)) {
                playlist.addUser(userToAdd);
                playlistRepository.save(playlist);
                return true;
            }
        }
        return false;
    }
}