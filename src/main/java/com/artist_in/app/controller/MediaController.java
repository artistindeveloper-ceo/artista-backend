package com.artist_in.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.filestorage.PresignedUploadResponse;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.service.MediaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
	private final MediaService mediaService;

	@PostMapping("/presign")
	public ResponseEntity<PresignedUploadResponse> presign(@RequestParam String mediaType, // profile | cover | post
			@RequestParam String contentType, // image/jpeg, video/mp4 etc.
			@RequestParam(defaultValue = "false") boolean isVideo) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("Presign requested: userId={}, mediaType={}, isVideo={}", userId, mediaType, isVideo);
		return ResponseEntity.ok(mediaService.generateUploadUrl(userId, mediaType, contentType, isVideo));
	}
}
