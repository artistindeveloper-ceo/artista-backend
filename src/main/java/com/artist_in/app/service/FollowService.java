package com.artist_in.app.service;

import org.springframework.data.domain.Pageable;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.follow.FollowRequestResponse;
import com.artist_in.app.dto.user.UserSummaryResponse;

public interface FollowService {

    String follow(Long followerId, Long targetId);

    void unfollow(Long followerId, Long targetId);

    FollowRequestResponse acceptFollowRequest(Long targetUserId, Long requestId);

    FollowRequestResponse rejectFollowRequest(Long targetUserId, Long requestId);

    PageResponse<FollowRequestResponse> getPendingRequestsForUser(Long userId, Pageable pageable);

    PageResponse<UserSummaryResponse> getFollowers(Long userId, Pageable pageable);

    PageResponse<UserSummaryResponse> getFollowing(Long userId, Pageable pageable);

}