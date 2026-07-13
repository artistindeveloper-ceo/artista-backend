package com.artist_in.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.entity.Follow;
import com.artist_in.app.entity.User;

public interface FollowRepository extends JpaRepository<Follow, Long> {

	boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

	boolean existsByFollowerAndFollowing(User follower, User following);

	Page<Follow> findByFollowing(User following, Pageable pageable);

	Page<Follow> findByFollower(User follower, Pageable pageable);

	long countByFollowing(User following);

	long countByFollower(User follower);

	void deleteByFollowerAndFollowing(User follower, User following);
}
