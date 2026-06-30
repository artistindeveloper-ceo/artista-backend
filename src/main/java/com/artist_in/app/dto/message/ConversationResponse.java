package com.artist_in.app.dto.message;

import java.time.Instant;

import com.artist_in.app.dto.user.UserSummaryResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
	private Long id;
	private UserSummaryResponse otherUser;
	private String lastMessagePreview;
	private Instant lastMessageAt;
	private long unreadCount;
}
