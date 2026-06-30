package com.artist_in.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.entity.JamSession;
import com.artist_in.app.entity.JamSessionParticipant;
import com.artist_in.app.entity.User;

public interface JamSessionParticipantRepository extends JpaRepository<JamSessionParticipant, Long> {

	Optional<JamSessionParticipant> findByJamSessionAndUser(JamSession jamSession, User user);

	List<JamSessionParticipant> findByJamSessionAndIsActiveTrue(JamSession jamSession);

	long countByJamSessionAndIsActiveTrue(JamSession jamSession);
}
