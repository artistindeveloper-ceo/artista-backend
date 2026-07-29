package com.artist_in.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.service.BusinessFollowService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessFollowController {

	private final BusinessFollowService businessFollowService;

	@PostMapping("/{businessId}/follow")
	public ResponseEntity<MessageResponse> follow(@PathVariable Long businessId,
			@AuthenticationPrincipal UserPrincipal currentUser) {
		String result = businessFollowService.follow(currentUser.getUser().getId(), businessId);
		return ResponseEntity.ok(MessageResponse.of(result));
	}

	@DeleteMapping("/{businessId}/follow")
	public ResponseEntity<MessageResponse> unfollow(@PathVariable Long businessId,
			@AuthenticationPrincipal UserPrincipal currentUser) {
		businessFollowService.unfollow(currentUser.getUser().getId(), businessId);
		return ResponseEntity.ok(MessageResponse.of("Unfollowed successfully."));
	}

	@GetMapping("/{businessId}/followers")
	public ResponseEntity<PageResponse<UserSummaryResponse>> getFollowers(@PathVariable Long businessId,
			Pageable pageable) {
		return ResponseEntity.ok(businessFollowService.getFollowers(businessId, pageable));
	}

	@GetMapping("/{businessId}/follow-status")
	public ResponseEntity<Boolean> isFollowing(@PathVariable Long businessId,
			@AuthenticationPrincipal UserPrincipal currentUser) {
		return ResponseEntity.ok(businessFollowService.isFollowing(currentUser.getUser().getId(), businessId));
	}
}