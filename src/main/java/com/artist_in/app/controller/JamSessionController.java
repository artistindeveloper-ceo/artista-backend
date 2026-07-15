package com.artist_in.app.controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.jam.AddSongToSetlistRequest;
import com.artist_in.app.dto.jam.ChangeCurrentSongRequest;
import com.artist_in.app.dto.jam.CreateJamSessionRequest;
import com.artist_in.app.dto.jam.JamParticipantResponse;
import com.artist_in.app.dto.jam.JamSessionEvent;
import com.artist_in.app.dto.jam.JamSessionResponse;
import com.artist_in.app.dto.jam.JamSessionSongResponse;
import com.artist_in.app.dto.transpose.TransposeDeltaRequest;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.service.JamSessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequestMapping("/api/v1/jam-sessions")
@RequiredArgsConstructor
public class JamSessionController {

	private final JamSessionService jamSessionService;

	@PostMapping
	public ResponseEntity<JamSessionResponse> createSession(@Valid @RequestBody CreateJamSessionRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("User {} creating jam session", userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(jamSessionService.createSession(userId, request));
	}

	@GetMapping("/{sessionId}")
	public ResponseEntity<JamSessionResponse> getSession(@PathVariable Long sessionId) {
		log.debug("Fetching jam session {}", sessionId);
		return ResponseEntity.ok(jamSessionService.getSession(sessionId));
	}

	@GetMapping("/by-invite-code/{inviteCode}")
	public ResponseEntity<JamSessionResponse> getSessionByInviteCode(@PathVariable String inviteCode) {
		log.debug("Fetching jam session by invite code={}", inviteCode);
		return ResponseEntity.ok(jamSessionService.getSessionByInviteCode(inviteCode));
	}

	@GetMapping("/mine")
	public ResponseEntity<PageResponse<JamSessionResponse>> getMySessions(Pageable pageable) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.debug("Fetching sessions for user {}, page={}", userId, pageable);
		return ResponseEntity.ok(jamSessionService.getMySessions(userId, pageable));
	}

	@PostMapping("/join/{inviteCode}")
	public ResponseEntity<JamSessionResponse> joinSession(@PathVariable String inviteCode) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("User {} joining session via invite code={}", userId, inviteCode);
		return ResponseEntity.ok(jamSessionService.joinSession(inviteCode, userId));
	}

	@PostMapping("/{sessionId}/leave")
	public ResponseEntity<MessageResponse> leaveSession(@PathVariable Long sessionId) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("User {} leaving session {}", userId, sessionId);
		jamSessionService.leaveSession(sessionId, userId);
		return ResponseEntity.ok(MessageResponse.of("Left the jam session."));
	}

	@PostMapping("/{sessionId}/start")
	public ResponseEntity<JamSessionResponse> startSession(@PathVariable Long sessionId) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("User {} starting session {}", userId, sessionId);
		return ResponseEntity.ok(jamSessionService.startSession(sessionId, userId));
	}

	@PostMapping("/{sessionId}/end")
	public ResponseEntity<JamSessionResponse> endSession(@PathVariable Long sessionId) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("User {} ending session {}", userId, sessionId);
		return ResponseEntity.ok(jamSessionService.endSession(sessionId, userId));
	}

	@PostMapping("/{sessionId}/invite/{inviteeId}")
	public ResponseEntity<MessageResponse> invite(@PathVariable Long sessionId, @PathVariable Long inviteeId) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("User {} inviting user {} to session {}", userId, inviteeId, sessionId);
		jamSessionService.inviteToSession(sessionId, userId, inviteeId);
		return ResponseEntity.ok(MessageResponse.of("Invitation sent."));
	}

	@PostMapping("/{sessionId}/setlist")
	public ResponseEntity<JamSessionSongResponse> addSongToSetlist(@PathVariable Long sessionId,
																   @Valid @RequestBody AddSongToSetlistRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("User {} adding song to setlist for session {}", userId, sessionId);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(jamSessionService.addSongToSetlist(sessionId, userId, request));
	}

	@DeleteMapping("/{sessionId}/setlist/{jamSessionSongId}")
	public ResponseEntity<MessageResponse> removeSongFromSetlist(@PathVariable Long sessionId,
																 @PathVariable Long jamSessionSongId) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("User {} removing song {} from setlist for session {}", userId, jamSessionSongId, sessionId);
		jamSessionService.removeSongFromSetlist(sessionId, userId, jamSessionSongId);
		return ResponseEntity.ok(MessageResponse.of("Song removed from setlist."));
	}

	/**
	 * The leader switches the currently active song; all participants receive the
	 * update instantly over /topic/jam-sessions/{sessionId}.
	 */
	@PostMapping("/{sessionId}/current-song")
	public ResponseEntity<JamSessionEvent> changeCurrentSong(@PathVariable Long sessionId,
															 @Valid @RequestBody ChangeCurrentSongRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("User {} changing current song for session {}", userId, sessionId);
		return ResponseEntity.ok(jamSessionService.changeCurrentSong(sessionId, userId, request));
	}

	/**
	 * The leader transposes the key of the currently active song in real time; all
	 * participants instantly see updated chords.
	 */
	@PostMapping("/{sessionId}/transpose/delta")
	public ResponseEntity<JamSessionEvent> transposeByDelta(@PathVariable Long sessionId,
															@RequestBody TransposeDeltaRequest request, @AuthenticationPrincipal UserPrincipal principal) {
		log.info("User {} transposing session {} by {} steps", principal.getId(), sessionId, request.getDeltaSteps());
		JamSessionEvent event = jamSessionService.transposeCurrentSongBy(sessionId, principal.getId(),
				request.getDeltaSteps());
		return ResponseEntity.ok(event);
	}

	@GetMapping("/{sessionId}/participants")
	public ResponseEntity<List<JamParticipantResponse>> getParticipants(@PathVariable Long sessionId) {
		log.debug("Fetching participants for session {}", sessionId);
		return ResponseEntity.ok(jamSessionService.getActiveParticipants(sessionId));
	}
}
