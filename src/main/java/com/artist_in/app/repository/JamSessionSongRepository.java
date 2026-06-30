package com.artist_in.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.entity.JamSession;
import com.artist_in.app.entity.JamSessionSong;

public interface JamSessionSongRepository extends JpaRepository<JamSessionSong, Long> {

	List<JamSessionSong> findByJamSessionOrderByPositionAsc(JamSession jamSession);

	Optional<JamSessionSong> findByJamSessionAndId(JamSession jamSession, Long id);

	long countByJamSession(JamSession jamSession);
}
