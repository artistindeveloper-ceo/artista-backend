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
public class ChatMessageResponse {
	private Long id;
	private Long conversationId;
	private UserSummaryResponse sender;
	private String content;
	private String attachmentUrl;
	private boolean isRead;
	private Instant createdAt;
}
