package com.artist_in.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artist_in.app.entity.Conversation;
import com.artist_in.app.entity.User;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	Optional<Conversation> findByUserAAndUserB(User userA, User userB);

	@Query("SELECT c FROM Conversation c WHERE c.userA = :user OR c.userB = :user "
			+ "ORDER BY CASE WHEN c.lastMessageAt IS NULL THEN 1 ELSE 0 END, c.lastMessageAt DESC")
	Page<Conversation> findAllForUser(@Param("user") User user, Pageable pageable);
}
