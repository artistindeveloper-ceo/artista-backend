package com.artist_in.app.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.jam.AddSongToSetlistRequest;
import com.artist_in.app.dto.jam.ChangeCurrentSongRequest;
import com.artist_in.app.dto.jam.CreateJamSessionRequest;
import com.artist_in.app.dto.jam.JamParticipantResponse;
import com.artist_in.app.dto.jam.JamSessionEvent;
import com.artist_in.app.dto.jam.JamSessionResponse;
import com.artist_in.app.dto.jam.JamSessionSongResponse;
import com.artist_in.app.dto.jam.TransposeRequest;
import com.artist_in.app.entity.JamSession;
import com.artist_in.app.entity.JamSessionParticipant;
import com.artist_in.app.entity.JamSessionSong;
import com.artist_in.app.entity.Song;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.JamSessionStatus;
import com.artist_in.app.enums.NotificationType;
import com.artist_in.app.enums.ParticipantRole;
import com.artist_in.app.exception.BadRequestException;
import com.artist_in.app.exception.ForbiddenException;
import com.artist_in.app.exception.ResourceNotFoundException;
import com.artist_in.app.repository.JamSessionParticipantRepository;
import com.artist_in.app.repository.JamSessionRepository;
import com.artist_in.app.repository.JamSessionSongRepository;
import com.artist_in.app.repository.SongRepository;
import com.artist_in.app.transpose.TransposeResult;
import com.artist_in.app.transpose.TransposeService;
import com.artist_in.app.util.UserMapper;
import com.artist_in.app.websocket.JamSessionEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JamSessionService {

	private static final String INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int INVITE_CODE_LENGTH = 6;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final JamSessionRepository jamSessionRepository;
	private final JamSessionSongRepository jamSessionSongRepository;
	private final JamSessionParticipantRepository participantRepository;
	private final SongRepository songRepository;
	private final UserService userService;
	private final NotificationService notificationService;
	private final JamSessionEventPublisher eventPublisher;
	// NEW — all transpose math/persistence/permission logic lives behind this now.
	private final TransposeService transposeService;

	@Transactional
	public JamSessionResponse createSession(Long leaderId, CreateJamSessionRequest request) {
		User leader = userService.getUserOrThrow(leaderId);

		JamSession session = JamSession.builder().name(request.getName()).description(request.getDescription())
				.leader(leader).status(JamSessionStatus.SCHEDULED).inviteCode(generateUniqueInviteCode())
				.scheduledStartAt(request.getScheduledStartAt())
				.isPrivate(request.getIsPrivate() == null || request.getIsPrivate()).build();

		session = jamSessionRepository.save(session);

		// Leader automatically joins as a participant with LEADER role.
		addParticipant(session, leader, ParticipantRole.LEADER);

		if (request.getInitialSongIds() != null && !request.getInitialSongIds().isEmpty()) {
			int position = 0;
			for (Long songId : request.getInitialSongIds()) {
				Song song = songRepository.findById(songId)
						.orElseThrow(() -> ResourceNotFoundException.of("Song", songId));
				JamSessionSong jss = JamSessionSong.builder().jamSession(session).song(song).position(position++)
						.build();
				jamSessionSongRepository.save(jss);
			}
		}

		return toResponse(session);
	}

	@Transactional(readOnly = true)
	public JamSessionResponse getSession(Long sessionId) {
		return toResponse(getSessionOrThrow(sessionId));
	}

	@Transactional(readOnly = true)
	public JamSessionResponse getSessionByInviteCode(String inviteCode) {
		JamSession session = jamSessionRepository.findByInviteCode(inviteCode.toUpperCase())
				.orElseThrow(() -> new ResourceNotFoundException("No jam session found with that invite code."));
		return toResponse(session);
	}

	@Transactional(readOnly = true)
	public PageResponse<JamSessionResponse> getMySessions(Long leaderId, Pageable pageable) {
		User leader = userService.getUserOrThrow(leaderId);
		Page<JamSession> page = jamSessionRepository.findByLeader(leader, pageable);
		return PageResponse.from(page, this::toResponse);
	}

	/**
	 * Join a session as a musician (or rejoin if previously left). Broadcasts
	 * PARTICIPANT_JOINED.
	 */
	@Transactional
	public JamSessionResponse joinSession(String inviteCode, Long userId) {
		JamSession session = jamSessionRepository.findByInviteCode(inviteCode.toUpperCase())
				.orElseThrow(() -> new ResourceNotFoundException("No jam session found with that invite code."));

		if (session.getStatus() == JamSessionStatus.ENDED || session.getStatus() == JamSessionStatus.CANCELLED) {
			throw new BadRequestException("This jam session has already ended.");
		}

		User user = userService.getUserOrThrow(userId);
		JamSessionParticipant participant = participantRepository.findByJamSessionAndUser(session, user)
				.map(existing -> {
					existing.setActive(true);
					existing.setLeftAt(null);
					existing.setJoinedAt(Instant.now());
					return participantRepository.save(existing);
				}).orElseGet(() -> addParticipant(session, user, ParticipantRole.MUSICIAN));

		JamSessionResponse response = toResponse(session);

		eventPublisher.publish(session.getId(),
				JamSessionEvent.builder().eventType(JamSessionEvent.EventType.PARTICIPANT_JOINED)
						.participant(toParticipantResponse(participant)).build());

		// Send the joining user a full state snapshot so their screen syncs
		// immediately.
		eventPublisher.publish(session.getId(),
				JamSessionEvent.builder().eventType(JamSessionEvent.EventType.SESSION_STATE_SNAPSHOT).session(response)
						.setlist(response.getSetlist()).currentSong(findCurrentSongResponse(session))
						.transposeOffset(session.getCurrentTransposeOffset()).build());

		return response;
	}

	@Transactional
	public void leaveSession(Long sessionId, Long userId) {
		JamSession session = getSessionOrThrow(sessionId);
		User user = userService.getUserOrThrow(userId);

		JamSessionParticipant participant = participantRepository.findByJamSessionAndUser(session, user)
				.orElseThrow(() -> new ResourceNotFoundException("You are not a participant in this session."));

		participant.setActive(false);
		participant.setLeftAt(Instant.now());
		participantRepository.save(participant);

		eventPublisher.publish(sessionId,
				JamSessionEvent.builder().eventType(JamSessionEvent.EventType.PARTICIPANT_LEFT)
						.participant(toParticipantResponse(participant)).build());
	}

	@Transactional
	public JamSessionResponse startSession(Long sessionId, Long requesterId) {
		JamSession session = getSessionOrThrow(sessionId);
		assertLeaderOrCoLeader(session, requesterId);

		session.setStatus(JamSessionStatus.LIVE);
		session.setStartedAt(Instant.now());
		session = jamSessionRepository.save(session);

		eventPublisher.publish(sessionId, JamSessionEvent.builder().eventType(JamSessionEvent.EventType.SESSION_STARTED)
				.message(session.getLeader().getDisplayName() + " started the jam session.").build());

		return toResponse(session);
	}

	@Transactional
	public JamSessionResponse endSession(Long sessionId, Long requesterId) {
		JamSession session = getSessionOrThrow(sessionId);
		assertLeaderOrCoLeader(session, requesterId);

		session.setStatus(JamSessionStatus.ENDED);
		session.setEndedAt(Instant.now());
		session = jamSessionRepository.save(session);

		eventPublisher.publish(sessionId, JamSessionEvent.builder().eventType(JamSessionEvent.EventType.SESSION_ENDED)
				.message("The jam session has ended.").build());

		return toResponse(session);
	}

	/**
	 * Leader invites another musician directly (creates a notification, doesn't
	 * auto-add them as participant).
	 */
	@Transactional
	public void inviteToSession(Long sessionId, Long requesterId, Long inviteeId) {
		JamSession session = getSessionOrThrow(sessionId);
		assertLeaderOrCoLeader(session, requesterId);
		User invitee = userService.getUserOrThrow(inviteeId);

		notificationService.notify(invitee, session.getLeader(), NotificationType.JAM_SESSION_INVITE, session.getId(),
				session.getLeader().getDisplayName() + " invited you to jam session \"" + session.getName() + "\".");
	}

	@Transactional
	public JamSessionSongResponse addSongToSetlist(Long sessionId, Long requesterId, AddSongToSetlistRequest request) {
		JamSession session = getSessionOrThrow(sessionId);
		assertLeaderOrCoLeader(session, requesterId);

		Song song = songRepository.findById(request.getSongId())
				.orElseThrow(() -> ResourceNotFoundException.of("Song", request.getSongId()));

		// ⬇️ NAYA CHECK — duplicate rokta hai
		boolean alreadyExists = jamSessionSongRepository.findByJamSessionOrderByPositionAsc(session).stream()
				.anyMatch(jss -> jss.getSong().getId().equals(song.getId()));
		if (alreadyExists) {
			throw new BadRequestException("This song is already in the setlist.");
		}

		int position = request.getPosition() != null ? request.getPosition()
				: (int) jamSessionSongRepository.countByJamSession(session);

		JamSessionSong jss = JamSessionSong.builder().jamSession(session).song(song).position(position).build();
		jss = jamSessionSongRepository.save(jss);

		broadcastSetlistUpdate(session);
		return toSetlistEntryResponse(jss);
	}

	@Transactional
	public void removeSongFromSetlist(Long sessionId, Long requesterId, Long jamSessionSongId) {
		JamSession session = getSessionOrThrow(sessionId);
		assertLeaderOrCoLeader(session, requesterId);

		JamSessionSong jss = jamSessionSongRepository.findByJamSessionAndId(session, jamSessionSongId)
				.orElseThrow(() -> ResourceNotFoundException.of("Setlist entry", jamSessionSongId));

		jamSessionSongRepository.delete(jss);

		if (session.getCurrentSongId() != null && session.getCurrentSongId().equals(jamSessionSongId)) {
			session.setCurrentSongId(null);
			jamSessionRepository.save(session);
		}

		broadcastSetlistUpdate(session);
	}

	/**
	 * The core "live jam session" action: the leader switches the currently active
	 * song. Every connected musician receives the new lyrics+chords (already
	 * transposed) over the WebSocket topic immediately.
	 *
	 * NOTE: this is a "song switch" concern, not a "transpose" concern — it just
	 * reuses whatever offset is already persisted for the target song (or an
	 * explicit override passed in this request). The actual chord math is delegated
	 * to TransposeService so ChordTransposer is never touched here.
	 */
	@Transactional
	public JamSessionEvent changeCurrentSong(Long sessionId, Long requesterId, ChangeCurrentSongRequest request) {
		JamSession session = getSessionOrThrow(sessionId);
		assertLeaderOrCoLeader(session, requesterId);

		JamSessionSong jss = jamSessionSongRepository.findByJamSessionAndId(session, request.getJamSessionSongId())
				.orElseThrow(() -> ResourceNotFoundException.of("Setlist entry", request.getJamSessionSongId()));

		if (request.getTransposeOffset() != null) {
			// Leader ne explicitly naya offset diya hai is song ke liye
			jss.setTransposeOffset(request.getTransposeOffset());
		} else {
			// Session ka jo bhi current offset chal raha hai, wahi naye song pe bhi carry karo
			jss.setTransposeOffset(session.getCurrentTransposeOffset());
		}
		jamSessionSongRepository.save(jss);

		session.setCurrentSongId(jss.getId());
		session.setCurrentTransposeOffset(jss.getTransposeOffset());
		jamSessionRepository.save(session);

		JamSessionEvent event = buildSongChangedEvent(jss);
		eventPublisher.publish(sessionId, event);
		return event;
	}

	/**
	 * Leader changes the transpose (key) of the currently playing song to an
	 * ABSOLUTE offset. Kept for backward compatibility with the existing
	 * TransposeRequest endpoint — delegates entirely to TransposeService now.
	 */
	@Transactional
	public JamSessionEvent transposeCurrentSong(Long sessionId, Long requesterId, TransposeRequest request) {
		return transposeService.transposeTo(sessionId, requesterId, request.getTransposeOffset());
	}

	/**
	 * NEW — the recommended way for the +/- buttons to call transpose. Client only
	 * sends a relative step (+1 / -1); the backend is the sole owner of the running
	 * offset, so there's no client-side "stale state" possible.
	 */
	@Transactional
	public JamSessionEvent transposeCurrentSongBy(Long sessionId, Long requesterId, int deltaSteps) {
		return transposeService.transposeBy(sessionId, requesterId, deltaSteps);
	}

	@Transactional(readOnly = true)
	public List<JamParticipantResponse> getActiveParticipants(Long sessionId) {
		JamSession session = getSessionOrThrow(sessionId);
		return participantRepository.findByJamSessionAndIsActiveTrue(session).stream().map(this::toParticipantResponse)
				.toList();
	}

	// ---------- internal helpers ----------

	private JamSessionParticipant addParticipant(JamSession session, User user, ParticipantRole role) {
		JamSessionParticipant participant = JamSessionParticipant.builder().jamSession(session).user(user).role(role)
				.joinedAt(Instant.now()).isActive(true).build();
		return participantRepository.save(participant);
	}

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

	private void broadcastSetlistUpdate(JamSession session) {
		List<JamSessionSongResponse> setlist = jamSessionSongRepository.findByJamSessionOrderByPositionAsc(session)
				.stream().map(this::toSetlistEntryResponse).toList();

		eventPublisher.publish(session.getId(), JamSessionEvent.builder()
				.eventType(JamSessionEvent.EventType.SETLIST_UPDATED).setlist(setlist).build());
	}

	private JamSessionEvent buildSongChangedEvent(JamSessionSong jss) {
		// Delegates the actual chord/key math to TransposeService — this class no
		// longer imports or calls ChordTransposer directly.
		TransposeResult result = transposeService.computeTransposedContent(jss);

		return JamSessionEvent.builder().eventType(JamSessionEvent.EventType.SONG_CHANGED)
				.currentSong(toSetlistEntryResponse(jss)).transposedKey(result.transposedKey())
				.transposedLyricsWithChords(result.transposedLyricsWithChords())
				.transposeOffset(jss.getTransposeOffset()).build();
	}

	private JamSessionSongResponse findCurrentSongResponse(JamSession session) {
		if (session.getCurrentSongId() == null) {
			return null;
		}
		return jamSessionSongRepository.findById(session.getCurrentSongId()).map(this::toSetlistEntryResponse)
				.orElse(null);
	}

	public JamSession getSessionOrThrow(Long sessionId) {
		return jamSessionRepository.findById(sessionId)
				.orElseThrow(() -> ResourceNotFoundException.of("JamSession", sessionId));
	}

	private String generateUniqueInviteCode() {
		for (int attempt = 0; attempt < 10; attempt++) {
			String candidate = randomInviteCode();
			if (!jamSessionRepository.existsByInviteCode(candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException("Could not generate a unique invite code after multiple attempts.");
	}

	private String randomInviteCode() {
		StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
		for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
			sb.append(INVITE_CODE_CHARS.charAt(RANDOM.nextInt(INVITE_CODE_CHARS.length())));
		}
		return sb.toString();
	}

	private JamSessionResponse toResponse(JamSession session) {
		List<JamSessionSongResponse> setlist = jamSessionSongRepository.findByJamSessionOrderByPositionAsc(session)
				.stream().map(this::toSetlistEntryResponse).toList();
		long activeCount = participantRepository.countByJamSessionAndIsActiveTrue(session);

		return JamSessionResponse.builder().id(session.getId()).name(session.getName())
				.description(session.getDescription()).leader(UserMapper.toSummary(session.getLeader()))
				.status(session.getStatus()).inviteCode(session.getInviteCode())
				.currentSongId(session.getCurrentSongId()).currentTransposeOffset(session.getCurrentTransposeOffset())
				.scheduledStartAt(session.getScheduledStartAt()).startedAt(session.getStartedAt())
				.endedAt(session.getEndedAt()).isPrivate(session.isPrivate()).activeParticipantCount(activeCount)
				.setlist(setlist).build();
	}

	private JamSessionSongResponse toSetlistEntryResponse(JamSessionSong jss) {
		return JamSessionSongResponse.builder().id(jss.getId()).songId(jss.getSong().getId())
				.title(jss.getSong().getTitle()).artist(jss.getSong().getArtist())
				.originalKey(jss.getSong().getOriginalKey()).position(jss.getPosition())
				.transposeOffset(jss.getTransposeOffset()).performed(jss.isPerformed()).build();
	}

	private JamParticipantResponse toParticipantResponse(JamSessionParticipant participant) {
		return JamParticipantResponse.builder().id(participant.getId())
				.user(UserMapper.toSummary(participant.getUser())).role(participant.getRole())
				.joinedAt(participant.getJoinedAt()).isActive(participant.isActive()).build();
	}
}