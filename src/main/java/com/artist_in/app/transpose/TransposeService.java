package com.artist_in.app.transpose;

import com.artist_in.app.dto.jam.JamSessionEvent;
import com.artist_in.app.entity.JamSessionSong;

/**
 * Owns all transpose business logic for a jam session: permission checks,
 * reading/writing the persisted per-song offset, and publishing the resulting
 * event. This is the ONLY entry point JamSessionService should call for
 * transpose-related actions — it should never touch
 * JamSessionSong.transposeOffset or ChordTransposer directly.
 */
public interface TransposeService {

	/**
	 * Applies a relative shift (e.g. +1 / -1 semitone) to the session's CURRENT
	 * song, on top of whatever offset is already persisted for it. The backend is
	 * the sole owner of the running total — callers (controller/Flutter) never need
	 * to know or send the current value.
	 */
	JamSessionEvent transposeBy(Long sessionId, Long requesterId, int deltaSteps);

	/**
	 * Sets an absolute offset for the session's current song. Kept for backward
	 * compatibility with the existing TransposeRequest endpoint.
	 */
	JamSessionEvent transposeTo(Long sessionId, Long requesterId, int absoluteOffset);

	/**
	 * Computes the transposed key + lyrics-with-chords for a setlist entry at its
	 * currently persisted offset, without changing anything. Used by
	 * JamSessionService when switching songs (SONG_CHANGED), so it never needs to
	 * touch ChordTransposer directly.
	 */
	TransposeResult computeTransposedContent(JamSessionSong jss);
}
