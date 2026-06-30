package com.artist_in.app.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.follow.FollowRequestResponse;
import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.entity.Follow;
import com.artist_in.app.entity.FollowRequest;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.FollowRequestStatus;
import com.artist_in.app.enums.NotificationType;
import com.artist_in.app.exception.BadRequestException;
import com.artist_in.app.exception.ResourceNotFoundException;
import com.artist_in.app.repository.FollowRepository;
import com.artist_in.app.repository.FollowRequestRepository;
import com.artist_in.app.util.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final FollowRequestRepository followRequestRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * Follow a public account directly, or create a pending follow request for
     * a private account.
     */
    @Transactional
    public String follow(Long followerId, Long targetId) {
        if (followerId.equals(targetId)) {
            throw new BadRequestException("You cannot follow yourself.");
        }
        User follower = userService.getUserOrThrow(followerId);
        User target = userService.getUserOrThrow(targetId);

        if (followRepository.existsByFollowerAndFollowing(follower, target)) {
            return "ALREADY_FOLLOWING";
        }

        if (target.isPrivate()) {
            followRequestRepository.findByRequesterAndTarget(follower, target)
                    .filter(req -> req.getStatus() == FollowRequestStatus.PENDING)
                    .ifPresent(req -> {
                        throw new BadRequestException("A follow request is already pending for this user.");
                    });

            FollowRequest request = followRequestRepository.findByRequesterAndTarget(follower, target)
                    .map(existing -> {
                        existing.setStatus(FollowRequestStatus.PENDING);
                        return existing;
                    })
                    .orElseGet(() -> FollowRequest.builder()
                            .requester(follower)
                            .target(target)
                            .status(FollowRequestStatus.PENDING)
                            .build());
            followRequestRepository.save(request);

            notificationService.notify(target, follower, NotificationType.FOLLOW_REQUEST_RECEIVED,
                    request.getId(), follower.getDisplayName() + " requested to follow you.");
            return "REQUEST_PENDING";
        }

        createFollowRelationship(follower, target);
        notificationService.notify(target, follower, NotificationType.NEW_FOLLOWER,
                follower.getId(), follower.getDisplayName() + " started following you.");
        return "FOLLOWING";
    }

    @Transactional
    public void unfollow(Long followerId, Long targetId) {
        User follower = userService.getUserOrThrow(followerId);
        User target = userService.getUserOrThrow(targetId);
        followRepository.deleteByFollowerAndFollowing(follower, target);
    }

    @Transactional
    public FollowRequestResponse acceptFollowRequest(Long targetUserId, Long requestId) {
        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> ResourceNotFoundException.of("FollowRequest", requestId));

        if (!request.getTarget().getId().equals(targetUserId)) {
            throw new BadRequestException("This follow request does not belong to you.");
        }
        if (request.getStatus() != FollowRequestStatus.PENDING) {
            throw new BadRequestException("This follow request has already been resolved.");
        }

        request.setStatus(FollowRequestStatus.ACCEPTED);
        followRequestRepository.save(request);

        createFollowRelationship(request.getRequester(), request.getTarget());

        notificationService.notify(request.getRequester(), request.getTarget(),
                NotificationType.FOLLOW_REQUEST_ACCEPTED, request.getTarget().getId(),
                request.getTarget().getDisplayName() + " accepted your follow request.");

        return toResponse(request);
    }

    @Transactional
    public FollowRequestResponse rejectFollowRequest(Long targetUserId, Long requestId) {
        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> ResourceNotFoundException.of("FollowRequest", requestId));

        if (!request.getTarget().getId().equals(targetUserId)) {
            throw new BadRequestException("This follow request does not belong to you.");
        }
        if (request.getStatus() != FollowRequestStatus.PENDING) {
            throw new BadRequestException("This follow request has already been resolved.");
        }

        request.setStatus(FollowRequestStatus.REJECTED);
        followRequestRepository.save(request);
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public PageResponse<FollowRequestResponse> getPendingRequestsForUser(Long userId, Pageable pageable) {
        User user = userService.getUserOrThrow(userId);
        Page<FollowRequest> page = followRequestRepository
                .findByTargetAndStatus(user, FollowRequestStatus.PENDING, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> getFollowers(Long userId, Pageable pageable) {
        User user = userService.getUserOrThrow(userId);
        Page<Follow> page = followRepository.findByFollowing(user, pageable);
        return PageResponse.from(page, follow -> UserMapper.toSummary(follow.getFollower()));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> getFollowing(Long userId, Pageable pageable) {
    	System.out.println("Sorting criteria received: " + pageable.getSort());
        User user = userService.getUserOrThrow(userId);
        Page<Follow> page = followRepository.findByFollower(user, pageable);
        return PageResponse.from(page, follow -> UserMapper.toSummary(follow.getFollowing()));
    }

    private void createFollowRelationship(User follower, User target) {
        Follow follow = Follow.builder()
                .follower(follower)
                .following(target)
                .createdAt(Instant.now())
                .build();
        followRepository.save(follow);
    }

    private FollowRequestResponse toResponse(FollowRequest request) {
        return FollowRequestResponse.builder()
                .id(request.getId())
                .requester(UserMapper.toSummary(request.getRequester()))
                .target(UserMapper.toSummary(request.getTarget()))
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
