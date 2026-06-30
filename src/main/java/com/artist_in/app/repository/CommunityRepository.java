package com.artist_in.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artist_in.app.entity.User;

public interface CommunityRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u " + "WHERE u.id <> :currentUserId " + "AND u.isActive = true " + "AND u.id NOT IN ("
			+ "SELECT f.following.id FROM Follow f " + "WHERE f.follower.id = :currentUserId" + ") "
			+ "ORDER BY u.createdAt DESC")
	Page<User> findDiscoverUsers(@Param("currentUserId") Long currentUserId, Pageable pageable);
}