package com.artist_in.app.serviceimpl;

import java.time.Instant;

import com.artist_in.app.service.AuthService;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManager authenticationManager;

	@Override
	@Transactional
	public AuthResponse register(RegisterRequest request) {
		log.info("Register attempt: username={}", request.getUsername());

		if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
			log.warn("Registration failed - username already taken: username={}", request.getUsername());
			throw new ConflictException("Username '" + request.getUsername() + "' is already taken.");
		}
		if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
			log.warn("Registration failed - email already in use: username={}", request.getUsername());
			throw new ConflictException("An account with this email already exists.");
		}

		User user = User.builder().username(request.getUsername()).email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getPassword())).displayName(request.getDisplayName())
				.role(Role.USER).isActive(true).build();

		user = userRepository.save(user);
		log.info("User registered successfully: userId={}, username={}", user.getId(), user.getUsername());

		UserPrincipal principal = new UserPrincipal(user);
		return buildAuthResponse(principal, user);
	}

	@Override
	@Transactional
	public AuthResponse login(LoginRequest request) {
		log.info("Login attempt: usernameOrEmail={}", request.getUsernameOrEmail());

		try {
			var authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
			UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
			User user = principal.getUser();
			user.setLastLoginAt(Instant.now());
			userRepository.save(user);

			log.info("Login successful: userId={}", user.getId());
			return buildAuthResponse(principal, user);
		} catch (Exception ex) {
			log.warn("Login failed: usernameOrEmail={}, reason={}", request.getUsernameOrEmail(), ex.getMessage());
			throw ex;
		}
	}

	@Override
	@Transactional
	public AuthResponse refresh(String refreshTokenValue) {
		log.debug("Refresh token request received");

		RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
				.orElseThrow(() -> {
					log.warn("Refresh failed - token not found");
					return new UnauthorizedException("Invalid refresh token.");
				});

		if (storedToken.isRevoked() || storedToken.isExpired()) {
			log.warn("Refresh failed - token revoked or expired: userId={}", storedToken.getUser().getId());
			throw new UnauthorizedException("Refresh token is expired or has been revoked. Please log in again.");
		}

		User user = storedToken.getUser();

		// Rotate: revoke the old refresh token and issue a brand new pair.
		storedToken.setRevoked(true);
		refreshTokenRepository.save(storedToken);

		log.info("Token refreshed successfully: userId={}", user.getId());

		UserPrincipal principal = new UserPrincipal(user);
		return buildAuthResponse(principal, user);
	}

	@Override
	@Transactional
	public void logout(String refreshTokenValue) {
		log.debug("Logout request received");

		refreshTokenRepository.findByToken(refreshTokenValue).ifPresentOrElse(token -> {
			token.setRevoked(true);
			refreshTokenRepository.save(token);
			log.info("Logout successful: userId={}", token.getUser().getId());
		}, () -> log.warn("Logout attempted with unknown/invalid refresh token"));
	}

	private AuthResponse buildAuthResponse(UserPrincipal principal, User user) {
		String accessToken = jwtTokenProvider.generateAccessToken(principal);
		String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();

		RefreshToken refreshToken = RefreshToken.builder().token(refreshTokenValue).user(user)
				.expiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()))
				.createdAt(Instant.now()).revoked(false).build();
		refreshTokenRepository.save(refreshToken);

		log.debug("Auth tokens generated for userId={}", user.getId());

		return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshTokenValue)
				.accessTokenExpiresInMs(jwtTokenProvider.getAccessTokenExpirationMs()).user(UserMapper.toSummary(user))
				.build();
	}
}