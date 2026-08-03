package com.artist_in.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.artist_in.app.entity.FollowRequest;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.FollowRequestStatus;

import io.lettuce.core.dynamic.annotation.Param;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

	Optional<FollowRequest> findByRequesterAndTarget(User requester, User target);

	Optional<FollowRequest> findByRequesterAndTargetAndStatus(User requester, User target, FollowRequestStatus status);

	Page<FollowRequest> findByTargetAndStatus(User target, FollowRequestStatus status, Pageable pageable);

	Page<FollowRequest> findByRequesterAndStatus(User requester, FollowRequestStatus status, Pageable pageable);

	long countByTargetAndStatus(User target, FollowRequestStatus status);

	/**
	 * Batch check: given a candidate list of target ids, return only the ones that
	 * requesterId has a PENDING follow request against. Used to avoid a query per
	 * row when rendering a list of users (search, discover, etc.).
	 */
	@Query("SELECT fr.target.id FROM FollowRequest fr WHERE fr.requester.id = :requesterId AND fr.target.id IN :targetIds AND fr.status = 'PENDING'")
	List<Long> findPendingTargetIdsByRequesterIdAndTargetIdIn(@Param("requesterId") Long requesterId,
			@Param("targetIds") List<Long> targetIds);
}
