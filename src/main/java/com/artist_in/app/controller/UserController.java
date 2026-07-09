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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final FileStorageService fileStorageService;

	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> getMyProfile() {
		Long userId = SecurityUtils.getCurrentUserId();
		return ResponseEntity.ok(userService.getProfileById(userId, userId));
	}

	@GetMapping("/{username}")
	public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String username) {
		Long viewerId = currentUserIdOrNull();
		return ResponseEntity.ok(userService.getProfile(username, viewerId));
	}

	@PutMapping("/me")
	public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		return ResponseEntity.ok(userService.updateProfile(userId, request));
	}

	@PostMapping("/me/password")
	public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		userService.changePassword(userId, request);
		return ResponseEntity.ok(MessageResponse.of("Password changed successfully."));
	}

	@PostMapping(value = "/me/profile-photo", consumes = "multipart/form-data")
	public ResponseEntity<MessageResponse> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {
		Long userId = SecurityUtils.getCurrentUserId();
		String url = fileStorageService.storeImage(file, FileStorageService.UploadCategory.PROFILE_PHOTOS);
		userService.updateProfilePhoto(userId, url);
		return ResponseEntity.ok(MessageResponse.of(url));
	}

	@PostMapping(value = "/me/cover-photo", consumes = "multipart/form-data")
	public ResponseEntity<MessageResponse> uploadCoverPhoto(@RequestParam("file") MultipartFile file) {
		Long userId = SecurityUtils.getCurrentUserId();
		String url = fileStorageService.storeImage(file, FileStorageService.UploadCategory.COVER_PHOTOS);
		userService.updateCoverPhoto(userId, url);
		return ResponseEntity.ok(MessageResponse.of(url));
	}

	@GetMapping("/search")
	public ResponseEntity<PageResponse<UserSummaryResponse>> search(@RequestParam String query,
			@AuthenticationPrincipal UserPrincipal currentUser, // ✅ ADD
			Pageable pageable) {
		return ResponseEntity.ok(userService.searchUsers(query, currentUser.getId(), pageable)); // ✅ pass id
	}

	private Long currentUserIdOrNull() {
		try {
			return SecurityUtils.getCurrentUserId();
		} catch (Exception ex) {
			return null;
		}
	}
}
