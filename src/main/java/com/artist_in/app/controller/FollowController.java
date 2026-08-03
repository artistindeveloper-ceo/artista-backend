package com.artist_in.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.follow.FollowActionResponse;
import com.artist_in.app.dto.follow.FollowRequestResponse;
import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.service.FollowService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor

public class FollowController {

	private final FollowService followService;

	@PostMapping("/users/{userId}/follow")
	public ResponseEntity<FollowActionResponse> follow(@PathVariable Long userId) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		log.info("User {} requested to follow user {}", currentUserId, userId);
		FollowActionResponse result = followService.follow(currentUserId, userId);
		log.info("Follow result for user {} -> {}: {}", currentUserId, userId, result.getStatus());
		return ResponseEntity.ok(result);
	}

	@DeleteMapping("/users/{userId}/follow")
	public ResponseEntity<FollowActionResponse> unfollow(@PathVariable Long userId) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		log.info("User {} requested to unfollow user {}", currentUserId, userId);
		FollowActionResponse result = followService.unfollow(currentUserId, userId);
		log.info("User {} unfollowed user {}", currentUserId, userId);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/follow-requests/{requestId}/accept")
	public ResponseEntity<FollowRequestResponse> acceptRequest(@PathVariable Long requestId) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		log.info("User {} accepting follow request {}", currentUserId, requestId);
		FollowRequestResponse response = followService.acceptFollowRequest(currentUserId, requestId);
		log.info("Follow request {} accepted by user {}", requestId, currentUserId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/follow-requests/{requestId}/reject")
	public ResponseEntity<FollowRequestResponse> rejectRequest(@PathVariable Long requestId) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		log.info("User {} rejecting follow request {}", currentUserId, requestId);
		FollowRequestResponse response = followService.rejectFollowRequest(currentUserId, requestId);
		log.info("Follow request {} rejected by user {}", requestId, currentUserId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/follow-requests/pending")
	public ResponseEntity<PageResponse<FollowRequestResponse>> getPendingRequests(Pageable pageable) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		log.debug("Fetching pending follow requests for user {}, page={}", currentUserId, pageable);
		return ResponseEntity.ok(followService.getPendingRequestsForUser(currentUserId, pageable));
	}

	@GetMapping("/users/{userId}/followers")
	public ResponseEntity<PageResponse<UserSummaryResponse>> getFollowers(@PathVariable Long userId,
			Pageable pageable) {
		log.debug("Fetching followers for user {}, page={}", userId, pageable);
		return ResponseEntity.ok(followService.getFollowers(userId, pageable));
	}

	@GetMapping("/users/{userId}/following")
	public ResponseEntity<PageResponse<UserSummaryResponse>> getFollowing(@PathVariable Long userId,
			Pageable pageable) {
		log.debug("Fetching following list for user {}, page={}", userId, pageable);
		return ResponseEntity.ok(followService.getFollowing(userId, pageable));
	}
}
