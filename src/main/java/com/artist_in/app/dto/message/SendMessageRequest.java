package com.artist_in.app.dto.message;

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
public class SendMessageRequest {

	@NotBlank(message = "Message content cannot be empty.")
	@Size(max = 4000, message = "Message must be at most 4000 characters.")
	private String content;

	private String attachmentUrl;
}
