package com.artist_in.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.auth.RegisterDeviceRequest;
import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.notification.NotificationResponse;
import com.artist_in.app.entity.User;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.service.NotificationService;
import com.artist_in.app.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
	private final NotificationService notificationService;
	private final UserService userService;

	@GetMapping
	public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(Pageable pageable) {
		User user = userService.getUserOrThrow(SecurityUtils.getCurrentUserId());
		log.debug("Fetching notifications for userId={}, page={}", user.getId(), pageable);
		return ResponseEntity.ok(notificationService.getNotifications(user, pageable));
	}

	@GetMapping("/unread-count")
	public ResponseEntity<java.util.Map<String, Long>> getUnreadCount() {
		User user = userService.getUserOrThrow(SecurityUtils.getCurrentUserId());
		log.debug("Fetching unread notification count for userId={}", user.getId());
		return ResponseEntity.ok(java.util.Map.of("unreadCount", notificationService.getUnreadCount(user)));
	}

	@PostMapping("/mark-all-read")
	public ResponseEntity<MessageResponse> markAllRead() {
		User user = userService.getUserOrThrow(SecurityUtils.getCurrentUserId());
		log.info("Marking all notifications as read for userId={}", user.getId());
		notificationService.markAllRead(user);
		return ResponseEntity.ok(MessageResponse.of("All notifications marked as read."));
	}

	@PostMapping("/register-device")
	public ResponseEntity<MessageResponse> registerDevice(@Valid @RequestBody RegisterDeviceRequest request) {
		User user = userService.getUserOrThrow(SecurityUtils.getCurrentUserId());
		log.info("Registering FCM token for userId={}", user.getId());
		notificationService.registerDeviceToken(user, request.getFcmToken());
		return ResponseEntity.ok(MessageResponse.of("Device registered for notifications."));
	}
}