package com.artist_in.app.websocket;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.artist_in.app.security.CustomUserDetailsService;
import com.artist_in.app.security.JwtTokenProvider;
import com.artist_in.app.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService userDetailsService;

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
			String token = resolveToken(accessor);
			if (token == null || !jwtTokenProvider.validateToken(token)) {
				throw new org.springframework.messaging.MessagingException("Missing or invalid JWT for STOMP CONNECT.");
			}
			Long userId = jwtTokenProvider.getUserIdFromToken(token);
			UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserById(userId);
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
					principal.getAuthorities());
			accessor.setUser(auth);
		}

		return message;
	}

	private String resolveToken(StompHeaderAccessor accessor) {
		String authHeader = accessor.getFirstNativeHeader("Authorization");
		if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
			return authHeader.substring(BEARER_PREFIX.length());
		}
		// Fallback: allow token to be sent as a plain native header named "token".
		return accessor.getFirstNativeHeader("token");
	}
}
