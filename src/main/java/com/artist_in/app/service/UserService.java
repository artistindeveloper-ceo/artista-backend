package com.artist_in.app.service;

import org.springframework.data.domain.Pageable;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.user.ChangePasswordRequest;
import com.artist_in.app.dto.user.UpdateProfileRequest;
import com.artist_in.app.dto.user.UserProfileResponse;
import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.entity.User;

public interface UserService {

    User getUserOrThrow(Long userId);

    User getUserByUsernameOrThrow(String username);

    UserProfileResponse getProfile(String username, Long viewerId);

    UserProfileResponse getProfileById(Long userId, Long viewerId);

    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    String updateProfilePhoto(Long userId, String photoUrl);

    String updateCoverPhoto(Long userId, String photoUrl);

    PageResponse<UserSummaryResponse> searchUsers(String query, Long currentUserId, Pageable pageable);

}