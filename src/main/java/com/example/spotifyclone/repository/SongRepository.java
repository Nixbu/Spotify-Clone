package com.example.spotifyclone.repository;

import com.example.spotifyclone.entity.Song;
import com.example.spotifyclone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByUploadedBy(User user);

    List<Song> findByArtistContainingIgnoreCase(String artist);

    List<Song> findByTitleContainingIgnoreCase(String title);

    List<Song> findByGenreContainingIgnoreCase(String genre);

    @Query("SELECT s FROM Song s WHERE s.title LIKE %:keyword% OR s.artist LIKE %:keyword% OR s.genre LIKE %:keyword%")
    List<Song> searchSongs(@Param("keyword") String keyword);

    @Query("SELECT s FROM Song s WHERE s.album.id = :albumId")
    List<Song> findByAlbumId(@Param("albumId") Long albumId);

    @Query("SELECT s FROM Song s ORDER BY s.id DESC")
    List<Song> findAllOrderByIdDesc();

    @Query("SELECT s FROM Song s JOIN s.favoriteByUsers u WHERE u.id = :userId")
    List<Song> findFavoritesByUserId(@Param("userId") Long userId);
}