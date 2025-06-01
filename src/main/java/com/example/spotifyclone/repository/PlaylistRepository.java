package com.example.spotifyclone.repository;

import com.example.spotifyclone.entity.Playlist;
import com.example.spotifyclone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByUser(User user);

    List<Playlist> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT p FROM Playlist p WHERE p.user.id = :userId")
    List<Playlist> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs WHERE p.id = :id")
    Playlist findByIdWithSongs(@Param("id") Long id);

    @Query("SELECT p FROM Playlist p WHERE p.user.id = :userId AND p.name LIKE %:name%")
    List<Playlist> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("name") String name);

    @Query("SELECT COUNT(p) FROM Playlist p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}