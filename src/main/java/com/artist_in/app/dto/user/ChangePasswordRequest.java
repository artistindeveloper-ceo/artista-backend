package com.artist_in.app.dto.user;

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
public class ChangePasswordRequest {

	@NotBlank(message = "Current password is required.")
	private String currentPassword;

	@NotBlank(message = "New password is required.")
	@Size(min = 8, max = 100, message = "New password must be at least 8 characters long.")
	private String newPassword;
}
