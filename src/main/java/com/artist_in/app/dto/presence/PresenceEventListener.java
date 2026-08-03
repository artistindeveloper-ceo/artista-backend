package com.artist_in.app.dto.presence;

import java.security.Principal;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.serviceimpl.PresenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bridges Spring's STOMP session lifecycle events to PresenceService.
 *
 * We use SessionConnectedEvent (fired AFTER the CONNECT frame is fully
 * processed, i.e. after StompAuthChannelInterceptor has already run and set the
 * authenticated user) rather than SessionConnectEvent, so the Principal is
 * guaranteed to be populated here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceEventListener {

	private final PresenceService presenceService;

	@EventListener
	public void handleSessionConnected(SessionConnectedEvent event) {
		SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
		Long userId = extractUserId(accessor.getUser());
		String sessionId = accessor.getSessionId();

		if (userId == null || sessionId == null) {
			log.warn("SessionConnectedEvent missing userId or sessionId — skipping presence update");
			return;
		}
		presenceService.userConnected(userId, sessionId);
	}

	@EventListener
	public void handleSessionDisconnect(SessionDisconnectEvent event) {
		Long userId = extractUserId(event.getUser()); // ← accessor की जगह event.getUser()
		String sessionId = event.getSessionId(); // ← accessor की जगह event.getSessionId()

		if (userId == null || sessionId == null) {
			log.warn("SessionDisconnectEvent missing userId or sessionId — skipping presence update");
			return;
		}
		presenceService.userDisconnected(userId, sessionId);
	}

	private Long extractUserId(Principal principal) {
		if (principal == null) {
			return null;
		}
		if (principal instanceof UsernamePasswordAuthenticationToken auth
				&& auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
			return userPrincipal.getId();
		}
		log.warn("Unexpected principal type on WebSocket session: {}", principal.getClass());
		return null;
	}
}
