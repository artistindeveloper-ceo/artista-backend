package com.artist_in.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.auth.AuthResponse;
import com.artist_in.app.dto.auth.LoginRequest;
import com.artist_in.app.dto.auth.RefreshTokenRequest;
import com.artist_in.app.dto.auth.RegisterRequest;
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt received",request.getUsernameOrEmail(),request.getPassword());
        AuthResponse response = authService.login(request);
        log.info("Login successful, userId={}", response.getUser().getId());
        return ResponseEntity.ok(response);
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
}
