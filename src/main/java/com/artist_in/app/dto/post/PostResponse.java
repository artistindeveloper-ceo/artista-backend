package com.artist_in.app.dto.post;

import java.time.Instant;

import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.enums.MediaType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
	private Long id;
	private UserSummaryResponse author;
	private String caption;
	private String mediaUrl;
	private String thumbnailUrl;
	private MediaType mediaType;
	private long likeCount;
	private long commentCount;
	private boolean likedByViewer;
	private Instant createdAt;
}
