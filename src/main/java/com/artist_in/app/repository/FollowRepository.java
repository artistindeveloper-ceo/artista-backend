package com.artist_in.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.entity.Follow;
import com.artist_in.app.entity.User;

public interface FollowRepository extends JpaRepository<Follow, Long> {

	Optional<Follow> findByFollowerAndFollowing(User follower, User following);

	boolean existsByFollowerAndFollowing(User follower, User following);

	Page<Follow> findByFollowing(User following, Pageable pageable);

	Page<Follow> findByFollower(User follower, Pageable pageable);

	long countByFollowing(User following);

	long countByFollower(User follower);

	void deleteByFollowerAndFollowing(User follower, User following);
}
