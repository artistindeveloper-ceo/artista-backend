package com.artist_in.app.dto.post;

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
public class CommentResponse {
	private Long id;
	private UserSummaryResponse author;
	private String content;
	private Long parentCommentId;
	private Instant createdAt;
}
