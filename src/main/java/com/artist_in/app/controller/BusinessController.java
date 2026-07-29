package com.artist_in.app.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.business.BusinessCreateRequest;
import com.artist_in.app.dto.business.BusinessResponse;
import com.artist_in.app.dto.business.BusinessUpdateRequest;
import com.artist_in.app.dto.message.ChatMessageResponse;
import com.artist_in.app.dto.message.SendMessageRequest;
import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.service.BusinessFollowService;
import com.artist_in.app.service.BusinessService;
import com.artist_in.app.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessController {

	private final BusinessService businessService;
	private final BusinessFollowService businessFollowService;
	private final MessageService messageService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BusinessResponse create(@AuthenticationPrincipal UserPrincipal currentUser,
			@Valid @RequestBody BusinessCreateRequest req) {
		return BusinessResponse.from(businessService.create(currentUser.getUser().getId(), req));
	}

	@GetMapping("/{id}")
	public BusinessResponse getById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
		var business = businessService.getById(id);
		long followerCount = businessFollowService.getFollowerCount(id);
		boolean isFollowedByViewer = currentUser != null
				&& businessFollowService.isFollowing(currentUser.getUser().getId(), id);
		return BusinessResponse.from(business, followerCount, isFollowedByViewer);
	}

	@PutMapping("/{id}")
	public BusinessResponse update(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser,
			@RequestBody BusinessUpdateRequest req) {
		return BusinessResponse.from(businessService.update(id, currentUser.getUser().getId(), req));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
		businessService.softDelete(id, currentUser.getUser().getId());
	}

	/**
	 * Message button on the business profile — routes to the business's primary
	 * owner via the existing MessageService, same conversation/DM system used for
	 * user-to-user chat. No separate business-chat system.
	 *
	 * Response includes otherUserId (the owner) so the Flutter client can open
	 * ChatScreen directly without a second lookup.
	 */
	@PostMapping("/{id}/message")
	public ResponseEntity<Map<String, Object>> message(@PathVariable Long id,
			@AuthenticationPrincipal UserPrincipal currentUser, @Valid @RequestBody SendMessageRequest request) {
		Long ownerId = businessService.getPrimaryOwnerUserId(id);
		ChatMessageResponse response = messageService.sendMessage(currentUser.getUser().getId(), ownerId, request);

		Map<String, Object> body = new HashMap<>();
		body.put("conversationId", response.getConversationId());
		body.put("otherUserId", ownerId);

		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}
}