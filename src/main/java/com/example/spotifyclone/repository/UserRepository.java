package com.example.spotifyclone.repository;

import com.example.spotifyclone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.favoriteSongs WHERE u.id = :id")
    Optional<User> findByIdWithFavorites(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.playlists WHERE u.id = :id")
    Optional<User> findByIdWithPlaylists(@Param("id") Long id);
}