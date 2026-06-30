package com.artist_in.app.service;

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
import com.artist_in.app.util.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	@Transactional
	public void notify(User recipient, User actor, NotificationType type, Long referenceId, String message) {
		// Don't notify users about their own actions (e.g. liking your own post).
		if (actor != null && actor.getId().equals(recipient.getId())) {
			return;
		}
		Notification notification = Notification.builder().recipient(recipient).actor(actor).type(type)
				.referenceId(referenceId).message(message).isRead(false).build();
		notificationRepository.save(notification);
	}

	@Transactional(readOnly = true)
	public PageResponse<NotificationResponse> getNotifications(User recipient, Pageable pageable) {
		Page<Notification> page = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient, pageable);
		return PageResponse.from(page, this::toResponse);
	}

	@Transactional(readOnly = true)
	public long getUnreadCount(User recipient) {
		return notificationRepository.countByRecipientAndIsReadFalse(recipient);
	}

	@Transactional
	public int markAllRead(User recipient) {
		return notificationRepository.markAllReadForRecipient(recipient);
	}

	private NotificationResponse toResponse(Notification notification) {
		return NotificationResponse.builder().id(notification.getId())
				.actor(notification.getActor() != null ? UserMapper.toSummary(notification.getActor()) : null)
				.type(notification.getType()).referenceId(notification.getReferenceId())
				.message(notification.getMessage()).isRead(notification.isRead()).createdAt(notification.getCreatedAt())
				.build();
	}
}
