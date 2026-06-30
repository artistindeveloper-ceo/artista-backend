package com.artist_in.app.dto.notification;

import java.time.Instant;

import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
	private Long id;
	private UserSummaryResponse actor;
	private NotificationType type;
	private Long referenceId;
	private String message;
	private boolean isRead;
	private Instant createdAt;
}
