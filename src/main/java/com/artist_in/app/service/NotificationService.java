package com.artist_in.app.service;

import org.springframework.data.domain.Pageable;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.notification.NotificationResponse;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.NotificationType;

public interface NotificationService {

	   void notify(User recipient, User actor, NotificationType type, Long referenceId, String message);
	    PageResponse<NotificationResponse> getNotifications(User recipient, Pageable pageable);
	    long getUnreadCount(User recipient);
	    int markAllRead(User recipient);

}