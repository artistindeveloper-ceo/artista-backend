package com.artist_in.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.entity.Song;
import com.artist_in.app.entity.User;

public interface SongRepository extends JpaRepository<Song, Long> {

	Page<Song> findByOwner(User owner, Pageable pageable);

	Page<Song> findByOwnerAndTitleContainingIgnoreCase(User owner, String title, Pageable pageable);

	Page<Song> findByIsPublicTrue(Pageable pageable);
}
