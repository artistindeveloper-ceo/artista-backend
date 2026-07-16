package com.artist_in.app.serviceimpl;

import java.time.Instant;

import com.artist_in.app.service.FollowService;
import com.artist_in.app.service.NotificationService;
import com.artist_in.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final FollowRequestRepository followRequestRepository;
    private final UserService userService;
    private final NotificationServiceImpl notificationService;

    /**
     * Follow a public account directly, or create a pending follow request for
     * a private account.
     */

    @Override
    @Transactional
    public String follow(Long followerId, Long targetId) {
        log.info("Follow requested: followerId={}, targetId={}", followerId, targetId);

        if (followerId.equals(targetId)) {
            log.warn("User {} attempted to follow themselves", followerId);
            throw new BadRequestException("You cannot follow yourself.");
        }
        User follower = userService.getUserOrThrow(followerId);
        User target = userService.getUserOrThrow(targetId);

        if (followRepository.existsByFollowerAndFollowing(follower, target)) {
            log.debug("User {} already follows user {}", followerId, targetId);
            return "ALREADY_FOLLOWING";
        }

        if (target.isPrivate()) {
            followRequestRepository.findByRequesterAndTarget(follower, target)
                    .filter(req -> req.getStatus() == FollowRequestStatus.PENDING)
                    .ifPresent(req -> {
                        log.warn("Duplicate follow request from user {} to user {}", followerId, targetId);
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
            log.info("Follow request created/updated: id={}, requester={}, target={}",
                    request.getId(), followerId, targetId);

            notificationService.notify(target, follower, NotificationType.FOLLOW_REQUEST_RECEIVED,
                    request.getId(), follower.getDisplayName() + " requested to follow you.");
            return "REQUEST_PENDING";
        }

        createFollowRelationship(follower, target);
        log.info("User {} started following user {}", followerId, targetId);
        notificationService.notify(target, follower, NotificationType.NEW_FOLLOWER,
                follower.getId(), follower.getDisplayName() + " started following you.");
        return "FOLLOWING";
    }

    @Override
    @Transactional
    public void unfollow(Long followerId, Long targetId) {
        log.info("Unfollow requested: followerId={}, targetId={}", followerId, targetId);
        User follower = userService.getUserOrThrow(followerId);
        User target = userService.getUserOrThrow(targetId);
        followRepository.deleteByFollowerAndFollowing(follower, target);
        log.info("User {} unfollowed user {}", followerId, targetId);
    }

    @Override
    @Transactional
    public FollowRequestResponse acceptFollowRequest(Long targetUserId, Long requestId) {
        log.info("Accept follow request: targetUserId={}, requestId={}", targetUserId, requestId);

        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Follow request {} not found", requestId);
                    return ResourceNotFoundException.of("FollowRequest", requestId);
                });

        if (!request.getTarget().getId().equals(targetUserId)) {
            log.warn("User {} attempted to accept a follow request {} that doesn't belong to them",
                    targetUserId, requestId);
            throw new BadRequestException("This follow request does not belong to you.");
        }
        if (request.getStatus() != FollowRequestStatus.PENDING) {
            log.warn("Follow request {} already resolved with status {}", requestId, request.getStatus());
            throw new BadRequestException("This follow request has already been resolved.");
        }

        request.setStatus(FollowRequestStatus.ACCEPTED);
        followRequestRepository.save(request);

        createFollowRelationship(request.getRequester(), request.getTarget());
        log.info("Follow request {} accepted, relationship created between {} and {}",
                requestId, request.getRequester().getId(), request.getTarget().getId());

        notificationService.notify(request.getRequester(), request.getTarget(),
                NotificationType.FOLLOW_REQUEST_ACCEPTED, request.getTarget().getId(),
                request.getTarget().getDisplayName() + " accepted your follow request.");

        return toResponse(request);
    }

    @Override
    @Transactional
    public FollowRequestResponse rejectFollowRequest(Long targetUserId, Long requestId) {
        log.info("Reject follow request: targetUserId={}, requestId={}", targetUserId, requestId);

        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Follow request {} not found", requestId);
                    return ResourceNotFoundException.of("FollowRequest", requestId);
                });

        if (!request.getTarget().getId().equals(targetUserId)) {
            log.warn("User {} attempted to reject a follow request {} that doesn't belong to them",
                    targetUserId, requestId);
            throw new BadRequestException("This follow request does not belong to you.");
        }
        if (request.getStatus() != FollowRequestStatus.PENDING) {
            log.warn("Follow request {} already resolved with status {}", requestId, request.getStatus());
            throw new BadRequestException("This follow request has already been resolved.");
        }

        request.setStatus(FollowRequestStatus.REJECTED);
        followRequestRepository.save(request);
        log.info("Follow request {} rejected", requestId);
        return toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FollowRequestResponse> getPendingRequestsForUser(Long userId, Pageable pageable) {
        log.debug("Fetching pending follow requests for user {}, page={}", userId, pageable);
        User user = userService.getUserOrThrow(userId);
        Page<FollowRequest> page = followRequestRepository
                .findByTargetAndStatus(user, FollowRequestStatus.PENDING, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> getFollowers(Long userId, Pageable pageable) {
        log.debug("Fetching followers for user {}, page={}", userId, pageable);
        User user = userService.getUserOrThrow(userId);
        Page<Follow> page = followRepository.findByFollowing(user, pageable);
        return PageResponse.from(page, follow -> UserMapper.toSummary(follow.getFollower()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> getFollowing(Long userId, Pageable pageable) {
        log.debug("Fetching following list for user {}, page={}, sort={}", userId, pageable, pageable.getSort());
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
