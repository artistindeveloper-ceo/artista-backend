package com.artist_in.app.serviceimpl;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.artist_in.app.dto.presence.PresencePublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tracks which users currently have an active WebSocket session and broadcasts
 * online/offline transitions over /topic/presence.
 *
 * A single user can have multiple concurrent STOMP sessions (e.g. phone +
 * tablet, or two browser tabs) — we only broadcast "offline" once the LAST
 * session for that user disconnects, so closing one tab doesn't wrongly mark
 * the user offline while they're still connected elsewhere.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceService {

    private final PresencePublisher presencePublisher;

    // userId -> set of active STOMP session ids for that user
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    // userId -> last time they were seen online (useful for "last seen" even
    // while offline)
    private final Map<Long, Instant> lastSeenAt = new ConcurrentHashMap<>();

    public void userConnected(Long userId, String stompSessionId) {
        Set<String> sessions = userSessions.computeIfAbsent(userId,
                k -> ConcurrentHashMap.newKeySet());
        boolean wasOffline = sessions.isEmpty();
        sessions.add(stompSessionId);
        lastSeenAt.put(userId, Instant.now());

        log.debug("User {} connected (session={}), total sessions={}", userId, stompSessionId, sessions.size());

        if (wasOffline) {
            presencePublisher.publish(userId, true);
            log.info("Broadcast presence: user {} is now ONLINE", userId);
        }
    }

    public void userDisconnected(Long userId, String stompSessionId) {
        Set<String> sessions = userSessions.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(stompSessionId);
        lastSeenAt.put(userId, Instant.now());

        log.debug("User {} disconnected (session={}), remaining sessions={}", userId, stompSessionId,
                sessions.size());

        if (sessions.isEmpty()) {
            userSessions.remove(userId);
            presencePublisher.publish(userId, false);
            log.info("Broadcast presence: user {} is now OFFLINE", userId);
        }
    }

    public boolean isOnline(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public Instant getLastSeenAt(Long userId) {
        return lastSeenAt.get(userId);
    }

    public Set<Long> getOnlineUserIds() {
        return Collections.unmodifiableSet(userSessions.keySet());
    }
}
