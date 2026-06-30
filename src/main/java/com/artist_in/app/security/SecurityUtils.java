package com.artist_in.app.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.artist_in.app.exception.UnauthorizedException;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
			throw new UnauthorizedException("No authenticated user found in security context.");
		}
		return principal.getId();
	}

	public static UserPrincipal getCurrentPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
			throw new UnauthorizedException("No authenticated user found in security context.");
		}
		return principal;
	}
}
