package com.artist_in.app.serviceimpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.notification.NotificationResponse;
import com.artist_in.app.entity.Notification;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.NotificationType;
import com.artist_in.app.repository.NotificationRepository;
import com.artist_in.app.repository.UserRepository;
import com.artist_in.app.service.NotificationService;
import com.artist_in.app.util.UserMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final FirebaseMessaging firebaseMessaging;

	@Override
	@Transactional
	public void notify(User recipient, User actor, NotificationType type, Long referenceId, String message) {
		// Don't notify users about their own actions (e.g. liking your own post).
		if (actor != null && actor.getId().equals(recipient.getId())) {
			log.debug("Skipping self-notification for userId={}, type={}", recipient.getId(), type);
			return;
		}
		Notification notification = Notification.builder().recipient(recipient).actor(actor).type(type)
				.referenceId(referenceId).message(message).isRead(false).build();
		notificationRepository.save(notification);
		log.info("Notification created: recipientId={}, actorId={}, type={}, referenceId={}", recipient.getId(),
				actor != null ? actor.getId() : null, type, referenceId);

		// ✅ Push notification bhejo agar recipient ka FCM token hai
		sendPushNotification(recipient, actor, type, message);
	}

	private void sendPushNotification(User recipient, User actor, NotificationType type, String message) {
		String token = recipient.getFcmToken();
		if (token == null || token.isBlank()) {
			log.debug("No FCM token for userId={}, skipping push", recipient.getId());
			return;
		}
		try {
			String title = actor != null ? actor.getDisplayName() : "Artist_in";
			Message fcmMessage = Message.builder().setToken(token).setNotification(
					com.google.firebase.messaging.Notification.builder().setTitle(title).setBody(message).build())
					.build();
			String response = firebaseMessaging.send(fcmMessage);
			log.info("Push notification sent to userId={}, response={}", recipient.getId(), response);
		} catch (Exception e) {
			log.error("Failed to send push notification to userId={}", recipient.getId(), e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<NotificationResponse> getNotifications(User recipient, Pageable pageable) {
		log.debug("Fetching notifications for recipientId={}, page={}", recipient.getId(), pageable);
		Page<Notification> page = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient, pageable);
		return PageResponse.from(page, this::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public long getUnreadCount(User recipient) {
		log.debug("Fetching unread notification count for recipientId={}", recipient.getId());
		return notificationRepository.countByRecipientAndIsReadFalse(recipient);
	}

	@Override
	@Transactional
	public int markAllRead(User recipient) {
		int updated = notificationRepository.markAllReadForRecipient(recipient);
		log.info("Marked {} notifications as read for recipientId={}", updated, recipient.getId());
		return updated;
	}

	@Override
	@Transactional
	public void registerDeviceToken(User user, String fcmToken) {
		user.setFcmToken(fcmToken);
		userRepository.save(user);
		log.info("FCM token registered for userId={}", user.getId());
	}

	private NotificationResponse toResponse(Notification notification) {
		return NotificationResponse.builder().id(notification.getId())
				.actor(notification.getActor() != null ? UserMapper.toSummary(notification.getActor()) : null)
				.type(notification.getType()).referenceId(notification.getReferenceId())
				.message(notification.getMessage()).isRead(notification.isRead()).createdAt(notification.getCreatedAt())
				.build();
	}
}