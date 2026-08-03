package com.artist_in.app.controller;

import com.artist_in.app.dto.auth.*;
import com.artist_in.app.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		log.info("Register request received");
		AuthResponse response = authService.register(request);
		log.info("User registered successfully, userId={}", response.getUser().getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest) {
		String ip = extractClientIp(httpRequest);
		AuthResponse response = authService.login(request, ip);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/google")
	public ResponseEntity<GoogleAuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request,
			HttpServletRequest httpRequest) {
		log.info("Google login request received");
		String ip = extractClientIp(httpRequest);
		GoogleAuthResponse response = authService.loginWithGoogle(request, ip);
		log.info("Google login processed, status={}", response.getStatus());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/google/complete-registration")
	public ResponseEntity<AuthResponse> completeGoogleRegistration(
			@Valid @RequestBody CompleteGoogleRegistrationRequest request, HttpServletRequest httpRequest) {
		log.info("Complete Google registration request received");
		String ip = extractClientIp(httpRequest);
		AuthResponse response = authService.completeGoogleRegistration(request, ip);
		log.info("Google registration completed, userId={}", response.getUser().getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	private String extractClientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim(); // proxy/load-balancer ke peeche pehla IP
		}
		return request.getRemoteAddr();
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		log.debug("Token refresh requested");
		AuthResponse response = authService.refresh(request.getRefreshToken());
		log.debug("Token refreshed successfully");
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
		log.info("Logout requested");
		authService.logout(request.getRefreshToken());
		log.info("Logout successful");
		return ResponseEntity.ok(MessageResponse.of("Logged out successfully."));
	}

	@PostMapping("/device/register")
	public ResponseEntity<MessageResponse> registerDevice(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody RegisterDeviceRequest request) {
		log.info("Device registration requested: userId={}", principal.getUser().getId());
		authService.registerDevice(principal.getUser().getId(), request);
		return ResponseEntity.ok(MessageResponse.of("Device registered successfully."));
	}
}