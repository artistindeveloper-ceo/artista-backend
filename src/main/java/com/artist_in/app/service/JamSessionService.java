package com.artist_in.app.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.jam.AddSongToSetlistRequest;
import com.artist_in.app.dto.jam.ChangeCurrentSongRequest;
import com.artist_in.app.dto.jam.CreateJamSessionRequest;
import com.artist_in.app.dto.jam.JamParticipantResponse;
import com.artist_in.app.dto.jam.JamSessionEvent;
import com.artist_in.app.dto.jam.JamSessionResponse;
import com.artist_in.app.dto.jam.JamSessionSongResponse;
import com.artist_in.app.dto.jam.TransposeRequest;

public interface JamSessionService {

	JamSessionResponse createSession(Long leaderId, CreateJamSessionRequest request);

	JamSessionResponse getSession(Long sessionId);

	JamSessionResponse getSessionByInviteCode(String inviteCode);

	PageResponse<JamSessionResponse> getMySessions(Long leaderId, Pageable pageable);

	JamSessionResponse joinSession(String inviteCode, Long userId);

	void leaveSession(Long sessionId, Long userId);

	JamSessionResponse startSession(Long sessionId, Long requesterId);

	JamSessionResponse endSession(Long sessionId, Long requesterId);

	void inviteToSession(Long sessionId, Long requesterId, Long inviteeId);

	JamSessionSongResponse addSongToSetlist(Long sessionId, Long requesterId, AddSongToSetlistRequest request);

	void removeSongFromSetlist(Long sessionId, Long requesterId, Long jamSessionSongId);

	JamSessionEvent changeCurrentSong(Long sessionId, Long requesterId, ChangeCurrentSongRequest request);

	JamSessionEvent transposeCurrentSong(Long sessionId, Long requesterId, TransposeRequest request);

	JamSessionEvent transposeCurrentSongBy(Long sessionId, Long requesterId, int deltaSteps);

	List<JamParticipantResponse> getActiveParticipants(Long sessionId);

}