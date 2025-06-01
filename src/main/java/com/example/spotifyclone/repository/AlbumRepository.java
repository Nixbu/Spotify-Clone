package com.example.spotifyclone.repository;

import com.example.spotifyclone.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByArtistContainingIgnoreCase(String artist);

    List<Album> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT a FROM Album a WHERE a.title LIKE %:keyword% OR a.artist LIKE %:keyword%")
    List<Album> searchAlbums(@Param("keyword") String keyword);

    @Query("SELECT a FROM Album a ORDER BY a.releaseDate DESC")
    List<Album> findAllOrderByReleaseDateDesc();

    @Query("SELECT a FROM Album a LEFT JOIN FETCH a.songs WHERE a.id = :id")
    Album findByIdWithSongs(@Param("id") Long id);

    @Query("SELECT a FROM Album a WHERE SIZE(a.songs) > 0")
    List<Album> findAlbumsWithSongs();
}