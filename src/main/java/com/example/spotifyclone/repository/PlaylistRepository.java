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

    List<Playlist> findByUsersContaining(User user);

    List<Playlist> findByUsersContainingOrderByCreatedAtDesc(User user);

    @Query("SELECT p FROM Playlist p JOIN p.users u WHERE u.id = :userId")
    List<Playlist> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs LEFT JOIN FETCH p.users WHERE p.id = :id")
    Playlist findByIdWithSongsAndUsers(@Param("id") Long id);

    @Query("SELECT p FROM Playlist p JOIN p.users u WHERE u.id = :userId AND lower(p.name) LIKE lower(concat('%', :name, '%'))")
    List<Playlist> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("name") String name);

    @Query("SELECT COUNT(p) FROM Playlist p JOIN p.users u WHERE u.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // Additional methods if you want to query by the direct user_id field
    List<Playlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(p) FROM Playlist p WHERE p.userId = :userId")
    long countByUserIdDirect(@Param("userId") Long userId);
}