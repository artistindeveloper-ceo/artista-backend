package com.artist_in.app.service;

import com.artist_in.app.dto.auth.AuthResponse;
import com.artist_in.app.dto.auth.CompleteGoogleRegistrationRequest;
import com.artist_in.app.dto.auth.GoogleAuthResponse;
import com.artist_in.app.dto.auth.GoogleLoginRequest;
import com.artist_in.app.dto.auth.LoginRequest;
import com.artist_in.app.dto.auth.RegisterDeviceRequest;
import com.artist_in.app.dto.auth.RegisterRequest;

public interface AuthService {
	AuthResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request, String ip);

	GoogleAuthResponse loginWithGoogle(GoogleLoginRequest request, String ip);

	AuthResponse completeGoogleRegistration(CompleteGoogleRegistrationRequest request, String ip);

	AuthResponse refresh(String refreshTokenValue);

	void logout(String refreshTokenValue);

	void registerDevice(Long userId, RegisterDeviceRequest request);
}