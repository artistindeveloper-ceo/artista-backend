package com.artist_in.app.dto.presence;

import java.time.Instant;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Broadcasts online/offline transitions to all clients subscribed to
 * /topic/presence. Mirrors JamSessionEventPublisher's shape so both publishers
 * stay consistent.
 */
@Component
@RequiredArgsConstructor
public class PresencePublisher {

	private static final String TOPIC = "/topic/presence";

	private final SimpMessagingTemplate messagingTemplate;

	public void publish(Long userId, boolean online) {
		PresenceEvent event = PresenceEvent.builder().userId(userId).online(online).timestamp(Instant.now()).build();
		messagingTemplate.convertAndSend(TOPIC, event);
	}

	public static String topic() {
		return TOPIC;
	}
}