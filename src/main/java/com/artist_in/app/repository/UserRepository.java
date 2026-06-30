package com.artist_in.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.artist_in.app.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsernameIgnoreCase(String username);

	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);

	Page<User> findByDisplayNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
			@Param("displayName") String displayName, @Param("username") String username, Pageable pageable);
}
