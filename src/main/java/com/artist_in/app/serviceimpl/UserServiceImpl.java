package com.artist_in.app.serviceimpl;

import com.artist_in.app.service.UserService;
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

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final FollowRequestRepository followRequestRepository;
	private final PostRepository postRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public User getUserOrThrow(Long userId) {
		log.debug("Fetching user with ID: {}", userId);

		return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
	}

	@Override
	public User getUserByUsernameOrThrow(String username) {
		log.debug("Fetching user with username: {}", username);

		return userRepository.findByUsernameIgnoreCase(username)
				.orElseThrow(() -> ResourceNotFoundException.of("User", username));
	}

	@Override
	@Transactional(readOnly = true)
	public UserProfileResponse getProfile(String username, Long viewerId) {

		log.info("Fetching profile for username: {}, viewerId: {}", username, viewerId);

		User target = getUserByUsernameOrThrow(username);

		UserProfileResponse response = buildProfileResponse(target, viewerId);

		log.info("Successfully fetched profile for username: {}", username);

		return response;
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getProfileById(Long userId, Long viewerId) {

		log.info("Fetching profile for userId: {}, viewerId: {}", userId, viewerId);

		User target = getUserOrThrow(userId);

		UserProfileResponse response = buildProfileResponse(target, viewerId);

		log.info("Successfully fetched profile for userId: {}", userId);

		return response;
	}

	private UserProfileResponse buildProfileResponse(User target, Long viewerId) {

		log.debug("Building profile response for targetUserId: {}, viewerId: {}", target.getId(), viewerId);

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

		log.debug("Profile stats for userId={} -> followers={}, following={}, posts={}", target.getId(), followerCount,
				followingCount, postCount);

		return UserProfileResponse.builder().id(target.getId()).username(target.getUsername())
				.email(viewingOwnProfile ? target.getEmail() : null).displayName(target.getDisplayName())
				.bio(target.getBio()).profilePhotoUrl(target.getProfilePhotoUrl())
				.coverPhotoUrl(target.getCoverPhotoUrl()).primaryInstrument(target.getPrimaryInstrument())
				.instruments(target.getInstruments()).roleType(target.getRoleType()).isPrivate(target.isPrivate())
				.followerCount(followerCount).followingCount(followingCount).postCount(postCount)
				.isFollowedByViewer(isFollowedByViewer).hasPendingFollowRequestFromViewer(hasPendingRequest)
				.createdAt(target.getCreatedAt()).build();
	}

	@Transactional
	public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {

		log.info("Updating profile for userId: {}", userId);

		User user = getUserOrThrow(userId);

		if (request.getDisplayName() != null) {
			user.setDisplayName(request.getDisplayName());
		}

		if (request.getBio() != null) {
			user.setBio(request.getBio());
		}

		if (request.getPrimaryInstrument() != null) {
			user.setPrimaryInstrument(request.getPrimaryInstrument());
		}

		if (request.getInstruments() != null) {
			user.setInstruments(request.getInstruments());
		}

		if (request.getIsPrivate() != null) {
			user.setPrivate(request.getIsPrivate());
		}

		user = userRepository.save(user);

		log.info("Profile updated successfully for userId: {}", userId);

		return buildProfileResponse(user, userId);
	}

	@Override
	@Transactional
	public void changePassword(Long userId, ChangePasswordRequest request) {

		log.info("Password change requested for userId: {}", userId);

		User user = getUserOrThrow(userId);

		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
			log.warn("Password change failed. Incorrect current password for userId: {}", userId);
			throw new BadRequestException("Current password is incorrect.");
		}

		user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);

		log.info("Password changed successfully for userId: {}", userId);
	}

	@Override
	@Transactional
	public String updateProfilePhoto(Long userId, String photoUrl) {

		log.info("Updating profile photo for userId: {}", userId);

		User user = getUserOrThrow(userId);

		user.setProfilePhotoUrl(photoUrl);
		userRepository.save(user);

		log.info("Profile photo updated successfully for userId: {}", userId);

		return photoUrl;
	}

	@Override
	@Transactional
	public String updateCoverPhoto(Long userId, String photoUrl) {

		log.info("Updating cover photo for userId: {}", userId);

		User user = getUserOrThrow(userId);

		user.setCoverPhotoUrl(photoUrl);
		userRepository.save(user);

		log.info("Cover photo updated successfully for userId: {}", userId);

		return photoUrl;
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<UserSummaryResponse> searchUsers(String query, Long currentUserId, Pageable pageable) {

		log.info("Searching users with query='{}', currentUserId={}, page={}, size={}", query, currentUserId,
				pageable.getPageNumber(), pageable.getPageSize());

		Page<User> users = userRepository.findByDisplayNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(query,
				query, pageable);

		User currentUser = getUserOrThrow(currentUserId);

		Page<UserSummaryResponse> mapped = users.map(user -> {

			boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(currentUserId, user.getId());

			boolean hasPending = followRequestRepository
					.findByRequesterAndTargetAndStatus(currentUser, user, FollowRequestStatus.PENDING).isPresent();

			return UserSummaryResponse.builder().id(user.getId()).username(user.getUsername())
					.displayName(user.getDisplayName()).profilePhotoUrl(user.getProfilePhotoUrl())
					.primaryInstrument(user.getPrimaryInstrument()).isFollowing(isFollowing)
					.hasPendingFollowRequest(hasPending).build();
		});

		log.info("User search completed. Query='{}', Total Results={}", query, mapped.getTotalElements());

		return PageResponse.from(mapped, u -> u);
	}
}
