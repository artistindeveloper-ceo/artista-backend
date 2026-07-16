package com.artist_in.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.user.ChangePasswordRequest;
import com.artist_in.app.dto.user.UpdateProfileRequest;
import com.artist_in.app.dto.user.UserProfileResponse;
import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.service.FileStorageService;
import com.artist_in.app.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

	private final UserService userService;
	private final FileStorageService fileStorageService;

	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> getMyProfile() {
		Long userId = SecurityUtils.getCurrentUserId();

		log.info("Received request to fetch profile of authenticated user. userId={}", userId);

		UserProfileResponse response = userService.getProfileById(userId, userId);

		log.info("Successfully fetched profile. userId={}", userId);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{username}")
	public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String username) {

		Long viewerId = currentUserIdOrNull();

		log.info("Received request to fetch profile. username={}, viewerId={}", username, viewerId);

		UserProfileResponse response = userService.getProfile(username, viewerId);

		log.info("Successfully fetched profile. username={}", username);

		return ResponseEntity.ok(response);
	}

	@PutMapping("/me")
	public ResponseEntity<UserProfileResponse> updateProfile(
			@Valid @RequestBody UpdateProfileRequest request) {

		Long userId = SecurityUtils.getCurrentUserId();

		log.info("Received request to update profile. userId={}", userId);

		UserProfileResponse response = userService.updateProfile(userId, request);

		log.info("Profile updated successfully. userId={}", userId);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/me/password")
	public ResponseEntity<MessageResponse> changePassword(
			@Valid @RequestBody ChangePasswordRequest request) {

		Long userId = SecurityUtils.getCurrentUserId();

		log.info("Received password change request. userId={}", userId);

		userService.changePassword(userId, request);

		log.info("Password changed successfully. userId={}", userId);

		return ResponseEntity.ok(MessageResponse.of("Password changed successfully."));
	}

	@PostMapping(value = "/me/profile-photo", consumes = "multipart/form-data")
	public ResponseEntity<MessageResponse> uploadProfilePhoto(
			@RequestParam("file") MultipartFile file) {

		Long userId = SecurityUtils.getCurrentUserId();

		log.info("Received profile photo upload request. userId={}, fileName={}",
				userId, file.getOriginalFilename());

		String url = fileStorageService.storeImage(
				file,
				FileStorageService.UploadCategory.PROFILE_PHOTOS);

		userService.updateProfilePhoto(userId, url);

		log.info("Profile photo uploaded successfully. userId={}, url={}", userId, url);

		return ResponseEntity.ok(MessageResponse.of(url));
	}

	@PostMapping(value = "/me/cover-photo", consumes = "multipart/form-data")
	public ResponseEntity<MessageResponse> uploadCoverPhoto(
			@RequestParam("file") MultipartFile file) {

		Long userId = SecurityUtils.getCurrentUserId();

		log.info("Received cover photo upload request. userId={}, fileName={}",
				userId, file.getOriginalFilename());

		String url = fileStorageService.storeImage(
				file,
				FileStorageService.UploadCategory.COVER_PHOTOS);

		userService.updateCoverPhoto(userId, url);

		log.info("Cover photo uploaded successfully. userId={}, url={}", userId, url);

		return ResponseEntity.ok(MessageResponse.of(url));
	}

	@GetMapping("/search")
	public ResponseEntity<PageResponse<UserSummaryResponse>> search(
			@RequestParam String query,
			@AuthenticationPrincipal UserPrincipal currentUser,
			Pageable pageable) {

		log.info("Received user search request. query='{}', userId={}, page={}, size={}",
				query,
				currentUser.getId(),
				pageable.getPageNumber(),
				pageable.getPageSize());

		PageResponse<UserSummaryResponse> response =
				userService.searchUsers(query, currentUser.getId(), pageable);

		log.info("User search completed successfully. query='{}'", query);

		return ResponseEntity.ok(response);
	}

	private Long currentUserIdOrNull() {
		try {
			return SecurityUtils.getCurrentUserId();
		} catch (Exception ex) {
			log.debug("Anonymous user accessed public profile endpoint.");
			return null;
		}
	}
}