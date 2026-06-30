package com.artist_in.app.service;

import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.auth.AuthResponse;
import com.artist_in.app.dto.auth.LoginRequest;
import com.artist_in.app.dto.auth.RegisterRequest;
import com.artist_in.app.entity.RefreshToken;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.Role;
import com.artist_in.app.exception.ConflictException;
import com.artist_in.app.exception.UnauthorizedException;
import com.artist_in.app.repository.RefreshTokenRepository;
import com.artist_in.app.repository.UserRepository;
import com.artist_in.app.security.JwtTokenProvider;
import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.util.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManager authenticationManager;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
			throw new ConflictException("Username '" + request.getUsername() + "' is already taken.");
		}
		if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
			throw new ConflictException("An account with this email already exists.");
		}

		User user = User.builder().username(request.getUsername()).email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getPassword())).displayName(request.getDisplayName())
				.role(Role.USER).isActive(true).build();

		user = userRepository.save(user);

		UserPrincipal principal = new UserPrincipal(user);
		return buildAuthResponse(principal, user);
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		var authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
		UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
		User user = principal.getUser();
		user.setLastLoginAt(Instant.now());
		userRepository.save(user);

		return buildAuthResponse(principal, user);
	}

	@Transactional
	public AuthResponse refresh(String refreshTokenValue) {
		RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
				.orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

		if (storedToken.isRevoked() || storedToken.isExpired()) {
			throw new UnauthorizedException("Refresh token is expired or has been revoked. Please log in again.");
		}

		User user = storedToken.getUser();

		// Rotate: revoke the old refresh token and issue a brand new pair.
		storedToken.setRevoked(true);
		refreshTokenRepository.save(storedToken);

		UserPrincipal principal = new UserPrincipal(user);
		return buildAuthResponse(principal, user);
	}

	@Transactional
	public void logout(String refreshTokenValue) {
		refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
			token.setRevoked(true);
			refreshTokenRepository.save(token);
		});
	}

	private AuthResponse buildAuthResponse(UserPrincipal principal, User user) {
		String accessToken = jwtTokenProvider.generateAccessToken(principal);
		String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();

		RefreshToken refreshToken = RefreshToken.builder().token(refreshTokenValue).user(user)
				.expiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()))
				.createdAt(Instant.now()).revoked(false).build();
		refreshTokenRepository.save(refreshToken);

		return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshTokenValue)
				.accessTokenExpiresInMs(jwtTokenProvider.getAccessTokenExpirationMs()).user(UserMapper.toSummary(user))
				.build();
	}
}
