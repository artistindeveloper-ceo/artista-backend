package com.artist_in.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.follow.FollowRequestResponse;
import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.service.FollowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FollowController {

	private final FollowService followService;

	@PostMapping("/users/{userId}/follow")
	public ResponseEntity<MessageResponse> follow(@PathVariable Long userId) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		String result = followService.follow(currentUserId, userId);
		return ResponseEntity.ok(MessageResponse.of(result));
	}

	@DeleteMapping("/users/{userId}/follow")
	public ResponseEntity<MessageResponse> unfollow(@PathVariable Long userId) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		followService.unfollow(currentUserId, userId);
		return ResponseEntity.ok(MessageResponse.of("Unfollowed successfully."));
	}

	@PostMapping("/follow-requests/{requestId}/accept")
	public ResponseEntity<FollowRequestResponse> acceptRequest(@PathVariable Long requestId) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		return ResponseEntity.ok(followService.acceptFollowRequest(currentUserId, requestId));
	}

	@PostMapping("/follow-requests/{requestId}/reject")
	public ResponseEntity<FollowRequestResponse> rejectRequest(@PathVariable Long requestId) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		return ResponseEntity.ok(followService.rejectFollowRequest(currentUserId, requestId));
	}

	@GetMapping("/follow-requests/pending")
	public ResponseEntity<PageResponse<FollowRequestResponse>> getPendingRequests(Pageable pageable) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		return ResponseEntity.ok(followService.getPendingRequestsForUser(currentUserId, pageable));
	}

	@GetMapping("/users/{userId}/followers")
	public ResponseEntity<PageResponse<UserSummaryResponse>> getFollowers(@PathVariable Long userId,
			Pageable pageable) {
		return ResponseEntity.ok(followService.getFollowers(userId, pageable));
	}

	@GetMapping("/users/{userId}/following")
	public ResponseEntity<PageResponse<UserSummaryResponse>> getFollowing(@PathVariable Long userId,
			Pageable pageable) {
		return ResponseEntity.ok(followService.getFollowing(userId, pageable));
	}
}
