package com.artist_in.app.websocket;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.artist_in.app.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

/**
 * Authenticates the WebSocket handshake using a JWT passed as a query
 * parameter, e.g. ws://host/ws?token=&lt;accessToken&gt; (SockJS/STOMP clients
 * commonly can't set custom headers during the initial handshake, so the token
 * travels as a query param here; the STOMP CONNECT frame is also validated
 * separately).
 */
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		if (request instanceof ServletServerHttpRequest servletRequest) {
			String token = servletRequest.getServletRequest().getParameter("token");
			if (token != null && jwtTokenProvider.validateToken(token)) {
				Long userId = jwtTokenProvider.getUserIdFromToken(token);
				attributes.put("userId", userId);
				return true;
			}
		}
		// Allow the handshake through even without a valid token here; the STOMP
		// CONNECT interceptor enforces authentication for actual message exchange.
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		// No-op
	}
}
