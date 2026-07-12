package com.artist_in.app.transpose;

import java.util.Optional;

import com.artist_in.app.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.jam.JamSessionEvent;
import com.artist_in.app.dto.jam.JamSessionSongResponse;
import com.artist_in.app.entity.JamSession;
import com.artist_in.app.entity.JamSessionParticipant;
import com.artist_in.app.entity.JamSessionSong;
import com.artist_in.app.entity.Song;
import com.artist_in.app.enums.ParticipantRole;
import com.artist_in.app.exception.BadRequestException;
import com.artist_in.app.exception.ForbiddenException;
import com.artist_in.app.exception.ResourceNotFoundException;
import com.artist_in.app.repository.JamSessionParticipantRepository;
import com.artist_in.app.repository.JamSessionRepository;
import com.artist_in.app.repository.JamSessionSongRepository;
import com.artist_in.app.websocket.JamSessionEventPublisher;

import lombok.RequiredArgsConstructor;

/**
 * All transpose calculation + persistence + broadcasting lives here. If a
 * transpose bug ever shows up again — wrong note, stale offset, wrong
 * permission behavior — THIS is the only file (plus ChordTransposer) that needs
 * checking. JamSessionService no longer touches any of this directly.
 */
@Service
@RequiredArgsConstructor
public class TransposeServiceImpl implements TransposeService {

	private final JamSessionRepository jamSessionRepository;
	private final JamSessionSongRepository jamSessionSongRepository;
	private final JamSessionParticipantRepository participantRepository;
	private final UserService userService;
	private final JamSessionEventPublisher eventPublisher;

	@Override
	@Transactional
	public JamSessionEvent transposeBy(Long sessionId, Long requesterId, int deltaSteps) {
		JamSession session = getSessionOrThrow(sessionId);
		assertLeaderOrCoLeader(session, requesterId);

		JamSessionSong jss = getCurrentSongOrThrow(session);
		int newOffset = jss.getTransposeOffset() + deltaSteps;

		return applyOffsetAndPublish(session, jss, newOffset);
	}

	@Override
	@Transactional
	public JamSessionEvent transposeTo(Long sessionId, Long requesterId, int absoluteOffset) {
		JamSession session = getSessionOrThrow(sessionId);
		assertLeaderOrCoLeader(session, requesterId);

		JamSessionSong jss = getCurrentSongOrThrow(session);

		return applyOffsetAndPublish(session, jss, absoluteOffset);
	}

	@Override
	public TransposeResult computeTransposedContent(JamSessionSong jss) {
		Song song = jss.getSong();
		return new TransposeResult(ChordTransposer.transposeKey(song.getOriginalKey(), jss.getTransposeOffset()),
				ChordTransposer.transposeLyricsWithChords(song.getLyricsWithChords(), jss.getTransposeOffset()));
	}

	// ---- internal helpers -------------------------------------------------

	private JamSessionEvent applyOffsetAndPublish(JamSession session, JamSessionSong jss, int newOffset) {
		jss.setTransposeOffset(newOffset);
		jamSessionSongRepository.save(jss);

		session.setCurrentTransposeOffset(newOffset);
		jamSessionRepository.save(session);

		TransposeResult result = computeTransposedContent(jss);

		JamSessionEvent event = JamSessionEvent.builder().eventType(JamSessionEvent.EventType.TRANSPOSE_CHANGED)
				.currentSong(toSetlistEntryResponse(jss)).transposedKey(result.transposedKey())
				.transposedLyricsWithChords(result.transposedLyricsWithChords()).transposeOffset(newOffset).build();

		eventPublisher.publish(session.getId(), event);
		return event;
	}

	private JamSessionSong getCurrentSongOrThrow(JamSession session) {
		if (session.getCurrentSongId() == null) {
			throw new BadRequestException("No song is currently active in this session.");
		}
		return jamSessionSongRepository.findById(session.getCurrentSongId())
				.orElseThrow(() -> ResourceNotFoundException.of("Setlist entry", session.getCurrentSongId()));
	}

	private JamSession getSessionOrThrow(Long sessionId) {
		return jamSessionRepository.findById(sessionId)
				.orElseThrow(() -> ResourceNotFoundException.of("JamSession", sessionId));
	}

	/**
	 * Duplicated from JamSessionService.assertLeaderOrCoLeader() on purpose — both
	 * classes need it and it's small. If you'd rather have one copy, pull this into
	 * a shared @Component (e.g. JamSessionAccessGuard) that both services inject;
	 * not done here to keep this change scoped to transpose only.
	 */
	private void assertLeaderOrCoLeader(JamSession session, Long userId) {
		if (session.getLeader().getId().equals(userId)) {
			return;
		}
		Optional<JamSessionParticipant> participant = participantRepository.findByJamSessionAndUser(session,
				userService.getUserOrThrow(userId));
		boolean isCoLeader = participant.map(p -> p.getRole() == ParticipantRole.CO_LEADER).orElse(false);
		if (!isCoLeader) {
			throw new ForbiddenException("Only the session leader or a co-leader can perform this action.");
		}
	}

	/**
	 * Same shape as JamSessionService.toSetlistEntryResponse() — kept identical so
	 * events stay consistent.
	 */
	private JamSessionSongResponse toSetlistEntryResponse(JamSessionSong jss) {
		return JamSessionSongResponse.builder().id(jss.getId()).songId(jss.getSong().getId())
				.title(jss.getSong().getTitle()).artist(jss.getSong().getArtist())
				.originalKey(jss.getSong().getOriginalKey()).position(jss.getPosition())
				.transposeOffset(jss.getTransposeOffset()).performed(jss.isPerformed()).build();
	}
}