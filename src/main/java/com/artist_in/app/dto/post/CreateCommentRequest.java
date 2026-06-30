package com.artist_in.app.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

	@NotBlank(message = "Comment content cannot be empty.")
	@Size(max = 1000, message = "Comment must be at most 1000 characters.")
	private String content;

	/** Optional - set when replying to another comment. */
	private Long parentCommentId;
}
