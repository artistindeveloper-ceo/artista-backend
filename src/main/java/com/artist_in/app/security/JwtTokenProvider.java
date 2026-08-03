package com.artist_in.app.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.artist_in.app.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

	private final JwtProperties jwtProperties;

	private static final String SIGNUP_TOKEN_TYPE = "google_signup";
	private static final long SIGNUP_TOKEN_EXPIRATION_MS = 10 * 60 * 1000; // 10 min

	private SecretKey signingKey() {
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
		} catch (IllegalArgumentException ex) {
			// Secret wasn't valid base64 - fall back to raw UTF-8 bytes
			keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
		}
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateAccessToken(UserPrincipal principal) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

		return Jwts.builder().subject(String.valueOf(principal.getId())).claim("username", principal.getUsername())
				.claim("type", "access").issuedAt(now).expiration(expiry).signWith(signingKey()).compact();
	}

	public String generateRefreshTokenValue() {
		// Opaque random refresh token value (not a JWT) - stored & validated
		// server-side.
		return java.util.UUID.randomUUID() + "-" + java.util.UUID.randomUUID();
	}

	public Long getUserIdFromToken(String token) {
		Claims claims = parseClaims(token);
		return Long.parseLong(claims.getSubject());
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (ExpiredJwtException ex) {
			log.debug("JWT expired: {}", ex.getMessage());
		} catch (JwtException | IllegalArgumentException ex) {
			log.debug("Invalid JWT: {}", ex.getMessage());
		}
		return false;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
	}

	public long getAccessTokenExpirationMs() {
		return jwtProperties.getAccessTokenExpirationMs();
	}

	public long getRefreshTokenExpirationMs() {
		return jwtProperties.getRefreshTokenExpirationMs();
	}

	public String generateSignupToken(String email, String googleSub, String name) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + SIGNUP_TOKEN_EXPIRATION_MS);
		return Jwts.builder().subject(email).claim("type", SIGNUP_TOKEN_TYPE).claim("googleSub", googleSub)
				.claim("name", name).issuedAt(now).expiration(expiry).signWith(signingKey()).compact();
	}

	public SignupTokenClaims parseSignupToken(String token) {
		Claims claims;
		try {
			claims = parseClaims(token);
		} catch (ExpiredJwtException ex) {
			throw new com.artist_in.app.exception.UnauthorizedException(
					"Signup session has expired. Please sign in with Google again.");
		} catch (JwtException | IllegalArgumentException ex) {
			throw new com.artist_in.app.exception.UnauthorizedException("Invalid signup token.");
		}

		if (!SIGNUP_TOKEN_TYPE.equals(claims.get("type", String.class))) {
			throw new com.artist_in.app.exception.UnauthorizedException("Invalid token type.");
		}

		return new SignupTokenClaims(claims.getSubject(), claims.get("googleSub", String.class),
				claims.get("name", String.class));
	}

	public record SignupTokenClaims(String email, String googleSub, String name) {
	}
}
