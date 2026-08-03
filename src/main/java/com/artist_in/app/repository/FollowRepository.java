package com.artist_in.app.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

	/**
	 * Batch check: given a candidate list of user ids, return only the ones that
	 * followerId already follows. Used to avoid an existsBy... query per row when
	 * rendering a list of users (search, discover, etc.).
	 */
	@Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId AND f.following.id IN :followingIds")
	List<Long> findFollowingIdsByFollowerIdAndFollowingIdIn(@Param("followerId") Long followerId,
			@Param("followingIds") List<Long> followingIds);
}