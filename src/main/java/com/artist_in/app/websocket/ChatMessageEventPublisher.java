package com.artist_in.app.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.artist_in.app.dto.message.ChatMessageResponse;

import lombok.RequiredArgsConstructor;

/**
 * Pushes new chat messages to the recipient's private user queue so their
 * client updates instantly without polling. The client subscribes to
 * /user/queue/messages (resolved by Spring to a unique per-session destination)
 * after authenticating with the username set on the STOMP Principal.
 */
@Component
@RequiredArgsConstructor
public class ChatMessageEventPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public void publishToUser(String username, ChatMessageResponse message) {
		messagingTemplate.convertAndSendToUser(username, "/queue/messages", message);
	}
}
