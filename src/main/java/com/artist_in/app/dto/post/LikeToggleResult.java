package com.artist_in.app.dto.post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LikeToggleResult {
	private boolean liked;
    private long likeCount;
}
