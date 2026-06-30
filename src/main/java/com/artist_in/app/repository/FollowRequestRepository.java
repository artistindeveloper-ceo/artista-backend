package com.artist_in.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.entity.FollowRequest;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.FollowRequestStatus;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

	Optional<FollowRequest> findByRequesterAndTarget(User requester, User target);

	Optional<FollowRequest> findByRequesterAndTargetAndStatus(User requester, User target, FollowRequestStatus status);

	Page<FollowRequest> findByTargetAndStatus(User target, FollowRequestStatus status, Pageable pageable);

	Page<FollowRequest> findByRequesterAndStatus(User requester, FollowRequestStatus status, Pageable pageable);

	long countByTargetAndStatus(User target, FollowRequestStatus status);
}
