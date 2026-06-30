package com.artist_in.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.user.ChangePasswordRequest;
import com.artist_in.app.dto.user.UpdateProfileRequest;
import com.artist_in.app.dto.user.UserProfileResponse;
import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.FollowRequestStatus;
import com.artist_in.app.exception.BadRequestException;
import com.artist_in.app.exception.ResourceNotFoundException;
import com.artist_in.app.repository.FollowRepository;
import com.artist_in.app.repository.FollowRequestRepository;
import com.artist_in.app.repository.PostRepository;
import com.artist_in.app.repository.UserRepository;
import com.artist_in.app.util.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final FollowRequestRepository followRequestRepository;
	private final PostRepository postRepository;
	private final PasswordEncoder passwordEncoder;

	public User getUserOrThrow(Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
	}

	public User getUserByUsernameOrThrow(String username) {
		return userRepository.findByUsernameIgnoreCase(username)
				.orElseThrow(() -> ResourceNotFoundException.of("User", username));
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getProfile(String username, Long viewerId) {
		User target = getUserByUsernameOrThrow(username);
		return buildProfileResponse(target, viewerId);
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getProfileById(Long userId, Long viewerId) {
		User target = getUserOrThrow(userId);
		return buildProfileResponse(target, viewerId);
	}

	private UserProfileResponse buildProfileResponse(User target, Long viewerId) {
		long followerCount = followRepository.countByFollowing(target);
		long followingCount = followRepository.countByFollower(target);
		long postCount = postRepository.findByAuthorAndIsArchivedFalseOrderByCreatedAtDesc(target, Pageable.unpaged())
				.getTotalElements();

		Boolean isFollowedByViewer = null;
		Boolean hasPendingRequest = null;

		boolean viewingOwnProfile = viewerId != null && viewerId.equals(target.getId());
		if (!viewingOwnProfile && viewerId != null) {
			User viewer = getUserOrThrow(viewerId);
			isFollowedByViewer = followRepository.existsByFollowerAndFollowing(viewer, target);
			hasPendingRequest = followRequestRepository
					.findByRequesterAndTargetAndStatus(viewer, target, FollowRequestStatus.PENDING).isPresent();
		}

		return UserProfileResponse.builder().id(target.getId()).username(target.getUsername())
				.email(viewingOwnProfile ? target.getEmail() : null).displayName(target.getDisplayName())
				.bio(target.getBio()).profilePhotoUrl(target.getProfilePhotoUrl())
				.coverPhotoUrl(target.getCoverPhotoUrl()).location(target.getLocation())
				.websiteUrl(target.getWebsiteUrl()).primaryInstrument(target.getPrimaryInstrument())
				.instruments(target.getInstruments()).genres(target.getGenres()).isPrivate(target.isPrivate())
				.followerCount(followerCount).followingCount(followingCount).postCount(postCount)
				.isFollowedByViewer(isFollowedByViewer).hasPendingFollowRequestFromViewer(hasPendingRequest)
				.createdAt(target.getCreatedAt()).build();
	}

	@Transactional
	public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
		User user = getUserOrThrow(userId);

		if (request.getDisplayName() != null) {
			user.setDisplayName(request.getDisplayName());
		}
		if (request.getBio() != null) {
			user.setBio(request.getBio());
		}
		if (request.getLocation() != null) {
			user.setLocation(request.getLocation());
		}
		if (request.getWebsiteUrl() != null) {
			user.setWebsiteUrl(request.getWebsiteUrl());
		}
		if (request.getPrimaryInstrument() != null) {
			user.setPrimaryInstrument(request.getPrimaryInstrument());
		}
		if (request.getInstruments() != null) {
			user.setInstruments(request.getInstruments());
		}
		if (request.getGenres() != null) {
			user.setGenres(request.getGenres());
		}
		if (request.getIsPrivate() != null) {
			user.setPrivate(request.getIsPrivate());
		}

		user = userRepository.save(user);
		return buildProfileResponse(user, userId);
	}

	@Transactional
	public void changePassword(Long userId, ChangePasswordRequest request) {
		User user = getUserOrThrow(userId);
		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
			throw new BadRequestException("Current password is incorrect.");
		}
		user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
	}

	@Transactional
	public String updateProfilePhoto(Long userId, String photoUrl) {
		User user = getUserOrThrow(userId);
		user.setProfilePhotoUrl(photoUrl);
		userRepository.save(user);
		return photoUrl;
	}

	@Transactional
	public String updateCoverPhoto(Long userId, String photoUrl) {
		User user = getUserOrThrow(userId);
		user.setCoverPhotoUrl(photoUrl);
		userRepository.save(user);
		return photoUrl;
	}

	@Transactional(readOnly = true)
	public PageResponse<UserSummaryResponse> searchUsers(String query, Pageable pageable) {
		Page<User> page = userRepository.findByDisplayNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(query,
				query, pageable);
		return PageResponse.from(page, UserMapper::toSummary);
	}
}
