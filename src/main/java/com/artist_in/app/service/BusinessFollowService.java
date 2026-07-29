package com.artist_in.app.service;

import org.springframework.data.domain.Pageable;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.user.UserSummaryResponse;

public interface BusinessFollowService {

	/**
	 * Always creates the relationship directly — businesses have no
	 * private/approval flow.
	 */
	String follow(Long followerId, Long businessId);

	void unfollow(Long followerId, Long businessId);

	boolean isFollowing(Long followerId, Long businessId);

	long getFollowerCount(Long businessId);

	PageResponse<UserSummaryResponse> getFollowers(Long businessId, Pageable pageable);
}
