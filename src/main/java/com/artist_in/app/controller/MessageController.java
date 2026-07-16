package com.artist_in.app.controller;

import com.artist_in.app.service.FileStorageService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.message.ChatMessageResponse;
import com.artist_in.app.dto.message.ConversationResponse;
import com.artist_in.app.dto.message.SendMessageRequest;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;




@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

	private final MessageService messageService;
	private final FileStorageService fileStorageService;

	@GetMapping("/conversations")
	public ResponseEntity<PageResponse<ConversationResponse>> getConversations(Pageable pageable) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.debug("Fetching conversations for userId={}, page={}", userId, pageable);
		return ResponseEntity.ok(messageService.getConversations(userId, pageable));
	}

	@GetMapping("/conversations/{conversationId}")
	public ResponseEntity<PageResponse<ChatMessageResponse>> getMessages(@PathVariable Long conversationId,
																		 Pageable pageable) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.debug("Fetching messages for conversationId={}, userId={}, page={}", conversationId, userId, pageable);
		return ResponseEntity.ok(messageService.getMessages(conversationId, userId, pageable));
	}

	@PostMapping("/conversations/{conversationId}/read")
	public ResponseEntity<MessageResponse> markRead(@PathVariable Long conversationId) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("Marking conversationId={} as read for userId={}", conversationId, userId);
		messageService.markConversationRead(conversationId, userId);
		return ResponseEntity.ok(MessageResponse.of("Conversation marked as read."));
	}

	/**
	 * Send a direct message to another user by their user id; creates the
	 * conversation if needed.
	 */
	@PostMapping("/users/{recipientId}")
	public ResponseEntity<ChatMessageResponse> sendMessage(@PathVariable Long recipientId,
														   @Valid @RequestBody SendMessageRequest request) {
		Long senderId = SecurityUtils.getCurrentUserId();
		log.info("Sending message from senderId={} to recipientId={}", senderId, recipientId);
		ChatMessageResponse response = messageService.sendMessage(senderId, recipientId, request);
		log.debug("Message sent, id={} in conversationId={}", response.getId(), response.getConversationId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping(value = "/attachments", consumes = "multipart/form-data")
	public ResponseEntity<MessageResponse> uploadAttachment(@RequestParam("file") MultipartFile file) {
		log.info("Uploading chat attachment: name={}, size={} bytes", file.getOriginalFilename(), file.getSize());
		FileStorageService.StoredMedia stored = fileStorageService.storeMedia(file,
				FileStorageService.UploadCategory.CHAT_ATTACHMENTS);
		log.info("Chat attachment stored: {}", stored.url());
		return ResponseEntity.ok(MessageResponse.of(stored.url()));
	}

	@GetMapping("/unread-count")
	public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
		log.debug("Fetching unread count for userId={}", principal.getId());
		long count = messageService.getTotalUnreadCount(principal.getId());
		return ResponseEntity.ok(count);
	}
}
