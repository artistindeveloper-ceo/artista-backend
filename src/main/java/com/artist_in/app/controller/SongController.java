package com.artist_in.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.jam.CreateSongRequest;
import com.artist_in.app.dto.jam.SongResponse;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.service.SongService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/songs")
@RequiredArgsConstructor
public class SongController {

	private final SongService songService;

	@PostMapping
	public ResponseEntity<SongResponse> createSong(@Valid @RequestBody CreateSongRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		return ResponseEntity.status(HttpStatus.CREATED).body(songService.createSong(userId, request));
	}

	@PutMapping("/{songId}")
	public ResponseEntity<SongResponse> updateSong(@PathVariable Long songId,
			@Valid @RequestBody CreateSongRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		return ResponseEntity.ok(songService.updateSong(songId, userId, request));
	}

	@DeleteMapping("/{songId}")
	public ResponseEntity<MessageResponse> deleteSong(@PathVariable Long songId) {
		Long userId = SecurityUtils.getCurrentUserId();
		songService.deleteSong(songId, userId);
		return ResponseEntity.ok(MessageResponse.of("Song deleted successfully."));
	}

	@GetMapping("/{songId}")
	public ResponseEntity<SongResponse> getSong(@PathVariable Long songId) {
		return ResponseEntity.ok(songService.getSong(songId));
	}

	@GetMapping("/mine")
	public ResponseEntity<PageResponse<SongResponse>> getMySongs(Pageable pageable) {
		Long userId = SecurityUtils.getCurrentUserId();
		return ResponseEntity.ok(songService.getMySongs(userId, pageable));
	}

	@GetMapping("/public")
	public ResponseEntity<PageResponse<SongResponse>> getPublicSongs(Pageable pageable) {
		return ResponseEntity.ok(songService.getPublicSongs(pageable));
	}
}
