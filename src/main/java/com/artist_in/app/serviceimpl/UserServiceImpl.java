package com.artist_in.app.serviceimpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.artist_in.app.entity.Business;
import com.artist_in.app.entity.Profile;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.AccountType;
import com.artist_in.app.enums.FollowRequestStatus;
import com.artist_in.app.exception.BadRequestException;
import com.artist_in.app.exception.ResourceNotFoundException;
import com.artist_in.app.repository.BusinessRepository;
import com.artist_in.app.repository.FollowRepository;
import com.artist_in.app.repository.FollowRequestRepository;
import com.artist_in.app.repository.PostRepository;
import com.artist_in.app.repository.ProfileRepository;
import com.artist_in.app.repository.UserRepository;
import com.artist_in.app.service.UserService;

import jakarta.persistence.EntityNotFoundException;
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
	private final ProfileRepository profileRepository;
	private final BusinessRepository businessRepository;

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

		// accountType ke hisab se INDIVIDUAL -> Profile.profileCategory,
		// BUSINESS -> Business.businessCategory se category resolve karte hain.
		// Flutter side ab guessing nahi karega, seedha accountType field use karega.
		String roleType = null; // backward-compat field, sirf INDIVIDUAL ke liye set hota hai
		String categoryCode = null;
		String categoryDisplayName = null;
		String businessName = null;

		AccountType accountType = target.getAccountType();

		if (accountType == AccountType.BUSINESS) {
			Business business = businessRepository.findFirstActiveByOwnerId(target.getId()).orElse(null);
			if (business != null) {
				businessName = business.getName();
				if (business.getBusinessCategory() != null) {
					categoryCode = business.getBusinessCategory().getCode();
					categoryDisplayName = business.getBusinessCategory().getDisplayName();
				}
			}
		} else {
			// INDIVIDUAL (ya accountType null — bare Google user jisne setup complete
			// nahi kiya, us case me Profile bhi nahi hoga aur sab null rahega)
			Profile profile = profileRepository.findById(target.getId()).orElse(null);
			if (profile != null && profile.getProfileCategory() != null) {
				roleType = profile.getProfileCategory().getCode();
				categoryCode = roleType;
				categoryDisplayName = profile.getProfileCategory().getCategoryName();
			}
		}

		log.debug("Profile stats for userId={} -> followers={}, following={}, posts={}", target.getId(), followerCount,
				followingCount, postCount);

		return UserProfileResponse.builder().id(target.getId()).username(target.getUsername())
				.email(viewingOwnProfile ? target.getEmail() : null).displayName(target.getDisplayName())
				.bio(target.getBio()).profilePhotoUrl(target.getProfilePhotoUrl())
				.coverPhotoUrl(target.getCoverPhotoUrl()).roleType(roleType)
				.accountType(accountType != null ? accountType.name() : null).categoryCode(categoryCode)
				.categoryDisplayName(categoryDisplayName).businessName(businessName).isPrivate(target.isPrivate())
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

		if (request.getIsPrivate() != null) {
			user.setPrivate(request.getIsPrivate());
		}

		user = userRepository.save(user);

		// primaryInstrument/instruments role-specific hai — Profile.details (JSONB)
		// me jaate hain, User pe nahi. Sirf INDIVIDUAL account ke paas Profile hota
		// hai.
		if (request.getPrimaryInstrument() != null || request.getInstruments() != null) {
			Profile profile = profileRepository.findById(userId).orElseThrow(() -> {
				log.warn("Profile update failed - no profile found (not an individual account?): userId={}", userId);
				return new EntityNotFoundException("Profile not found for userId: " + userId);
			});

			Map<String, Object> details = profile.getDetails() != null ? profile.getDetails() : new HashMap<>();

			if (request.getPrimaryInstrument() != null) {
				details.put("primaryInstrument", request.getPrimaryInstrument());
			}
			if (request.getInstruments() != null) {
				details.put("instruments", request.getInstruments());
			}

			profile.setDetails(details);
			profileRepository.save(profile);
		}

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

		List<Long> userIds = users.getContent().stream().map(User::getId).collect(Collectors.toList());

		// N+1 FIX: pehle har user ke liye 2 alag queries chal rahi thi (isFollowing +
		// hasPending). Ab dono ek-ek batch query me nikal li jaati hain, result ID
		// ke Set me daal ke O(1) lookup kiya jaata hai loop ke andar.
		Set<Long> followingIds = userIds.isEmpty() ? Set.of()
				: new HashSet<>(followRepository.findFollowingIdsByFollowerIdAndFollowingIdIn(currentUserId, userIds));

		Set<Long> pendingTargetIds = userIds.isEmpty() ? Set.of()
				: new HashSet<>(
						followRequestRepository.findPendingTargetIdsByRequesterIdAndTargetIdIn(currentUserId, userIds));

		Page<UserSummaryResponse> mapped = users.map(user -> UserSummaryResponse.builder().id(user.getId())
				.username(user.getUsername()).displayName(user.getDisplayName())
				.profilePhotoUrl(user.getProfilePhotoUrl()).isFollowing(followingIds.contains(user.getId()))
				.hasPendingFollowRequest(pendingTargetIds.contains(user.getId())).build());

		log.info("User search completed. Query='{}', Total Results={}", query, mapped.getTotalElements());

		return PageResponse.from(mapped, u -> u);
	}
}