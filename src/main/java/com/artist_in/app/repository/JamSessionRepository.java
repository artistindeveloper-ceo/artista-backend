package com.artist_in.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.entity.JamSession;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.JamSessionStatus;

public interface JamSessionRepository extends JpaRepository<JamSession, Long> {

	Optional<JamSession> findByInviteCode(String inviteCode);

	Page<JamSession> findByLeader(User leader, Pageable pageable);

	Page<JamSession> findByStatus(JamSessionStatus status, Pageable pageable);

	boolean existsByInviteCode(String inviteCode);
}
