package com.artist_in.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeviceRequest {

	@NotBlank(message = "deviceId is required")
	private String deviceId;

	@NotBlank(message = "fcmToken is required")
	private String fcmToken;
}
