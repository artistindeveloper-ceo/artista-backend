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
import com.artist_in.app.service.NotificationService;
import com.artist_in.app.util.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;
	private final PushNotificationSender pushNotificationSender;

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

		// ✅ Push notification background thread pe jayega — transaction/response block
		// nahi hoga.
		// NOTE: actor.getDisplayName() aur recipient.getId() yahin extract kar liye —
		// User entity ko async thread me pass karna risky hai (lazy-loaded fields,
		// session already closed).
		String actorDisplayName = actor != null ? actor.getDisplayName() : null;
		pushNotificationSender.sendPushNotification(recipient.getId(), actorDisplayName, message);
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

	private NotificationResponse toResponse(Notification notification) {
		return NotificationResponse.builder().id(notification.getId())
				.actor(notification.getActor() != null ? UserMapper.toSummary(notification.getActor()) : null)
				.type(notification.getType()).referenceId(notification.getReferenceId())
				.message(notification.getMessage()).isRead(notification.isRead()).createdAt(notification.getCreatedAt())
				.build();
	}
}