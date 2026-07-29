package com.artist_in.app.serviceimpl;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.entity.Business;
import com.artist_in.app.entity.BusinessFollow;
import com.artist_in.app.entity.User;
import com.artist_in.app.exception.BadRequestException;
import com.artist_in.app.exception.ResourceNotFoundException;
import com.artist_in.app.repository.BusinessFollowRepository;
import com.artist_in.app.repository.BusinessRepository;
import com.artist_in.app.repository.UserRepository;
import com.artist_in.app.service.BusinessFollowService;
import com.artist_in.app.util.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessFollowServiceImpl implements BusinessFollowService {

	private final BusinessFollowRepository businessFollowRepository;
	private final BusinessRepository businessRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public String follow(Long followerId, Long businessId) {
		log.info("Follow requested: followerId={}, businessId={}", followerId, businessId);

		User follower = userRepository.findById(followerId)
				.orElseThrow(() -> ResourceNotFoundException.of("User", followerId));
		Business business = businessRepository.findActiveByIdWithCity(businessId)
				.orElseThrow(() -> ResourceNotFoundException.of("Business", businessId));

		if (businessFollowRepository.existsByFollower_IdAndBusiness_Id(followerId, businessId)) {
			log.debug("User {} already follows business {}", followerId, businessId);
			return "ALREADY_FOLLOWING";
		}

		// No private/request flow — businesses are always public, follow immediately.
		businessFollowRepository
				.save(BusinessFollow.builder().follower(follower).business(business).createdAt(Instant.now()).build());

		log.info("User {} started following business {}", followerId, businessId);
		return "FOLLOWING";
	}

	@Override
	@Transactional
	public void unfollow(Long followerId, Long businessId) {
		log.info("Unfollow requested: followerId={}, businessId={}", followerId, businessId);
		if (!businessFollowRepository.existsByFollower_IdAndBusiness_Id(followerId, businessId)) {
			throw new BadRequestException("You are not following this business.");
		}
		businessFollowRepository.deleteByFollower_IdAndBusiness_Id(followerId, businessId);
		log.info("User {} unfollowed business {}", followerId, businessId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isFollowing(Long followerId, Long businessId) {
		return businessFollowRepository.existsByFollower_IdAndBusiness_Id(followerId, businessId);
	}

	@Override
	@Transactional(readOnly = true)
	public long getFollowerCount(Long businessId) {
		return businessFollowRepository.countByBusiness_Id(businessId);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<UserSummaryResponse> getFollowers(Long businessId, Pageable pageable) {
		Page<BusinessFollow> page = businessFollowRepository.findFollowersOfBusiness(businessId, pageable);
		return PageResponse.from(page, follow -> UserMapper.toSummary(follow.getFollower()));
	}
}