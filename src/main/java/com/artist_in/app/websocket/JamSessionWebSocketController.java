package com.artist_in.app.websocket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.artist_in.app.dto.jam.ChangeCurrentSongRequest;
import com.artist_in.app.dto.jam.TransposeRequest;
import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.service.JamSessionService;

import lombok.RequiredArgsConstructor;

/**
 * STOMP destinations for live jam session control. Clients send to
 * /app/jam-sessions/{sessionId}/... ; the service publishes the resulting state
 * to /topic/jam-sessions/{sessionId} for every subscriber.
 *
 * Equivalent REST endpoints exist under JamSessionController for clients that
 * prefer plain HTTP plus a separate WebSocket subscription for receiving
 * updates only.
 */
@Controller
@RequiredArgsConstructor
public class JamSessionWebSocketController {

	private final JamSessionService jamSessionService;

	@MessageMapping("/jam-sessions/{sessionId}/change-song")
	public void changeSong(@DestinationVariable Long sessionId, @Payload ChangeCurrentSongRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		jamSessionService.changeCurrentSong(sessionId, principal.getId(), request);
	}

	@MessageMapping("/jam-sessions/{sessionId}/transpose")
	public void transpose(@DestinationVariable Long sessionId, @Payload TransposeRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		jamSessionService.transposeCurrentSong(sessionId, principal.getId(), request);
	}
}
