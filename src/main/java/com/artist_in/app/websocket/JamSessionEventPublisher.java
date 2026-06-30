package com.artist_in.app.websocket;

import java.time.Instant;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.artist_in.app.dto.jam.JamSessionEvent;

import lombok.RequiredArgsConstructor;

/**
 * Broadcasts jam session state changes to all clients subscribed to
 * /topic/jam-sessions/{sessionId}. The leader's actions (song change,
 * transpose, setlist edits) flow through JamSessionService, which calls this
 * publisher so every musician's screen updates immediately.
 */
@Component
@RequiredArgsConstructor
public class JamSessionEventPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public void publish(Long jamSessionId, JamSessionEvent event) {
		event.setJamSessionId(jamSessionId);
		if (event.getTimestamp() == null) {
			event.setTimestamp(Instant.now());
		}
		messagingTemplate.convertAndSend(topicFor(jamSessionId), event);
	}

	public static String topicFor(Long jamSessionId) {
		return "/topic/jam-sessions/" + jamSessionId;
	}
}
