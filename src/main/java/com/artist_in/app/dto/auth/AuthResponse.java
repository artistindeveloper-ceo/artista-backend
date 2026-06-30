package com.artist_in.app.dto.auth;

import com.artist_in.app.dto.user.UserSummaryResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
	private String accessToken;
	private String refreshToken;
	private long accessTokenExpiresInMs;
	private UserSummaryResponse user;
}
