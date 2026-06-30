package com.artist_in.app.dto.jam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Generic real-time event broadcast over the jam session's WebSocket topic
 * (/topic/jam-sessions/{sessionId}). The frontend switches behavior based on
 * `eventType`. Only the fields relevant to that event type will be populated.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JamSessionEvent {

    public enum EventType {
        SONG_CHANGED,
        TRANSPOSE_CHANGED,
        SETLIST_UPDATED,
        SESSION_STARTED,
        SESSION_ENDED,
        PARTICIPANT_JOINED,
        PARTICIPANT_LEFT,
        SESSION_STATE_SNAPSHOT
    }

    private EventType eventType;
    private Long jamSessionId;
    private Instant timestamp;

    /** Populated for SONG_CHANGED / SESSION_STATE_SNAPSHOT. */
    private JamSessionSongResponse currentSong;
    private String transposedKey;
    private String transposedLyricsWithChords;
    private int transposeOffset;

    /** Populated for SETLIST_UPDATED / SESSION_STATE_SNAPSHOT. */
    private java.util.List<JamSessionSongResponse> setlist;

    /** Populated for PARTICIPANT_JOINED / PARTICIPANT_LEFT. */
    private JamParticipantResponse participant;

    /** Populated for SESSION_STATE_SNAPSHOT, sent to a user right after they join. */
    private JamSessionResponse session;

    private String message;
}
