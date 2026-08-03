package com.artist_in.app.util;

import com.artist_in.app.dto.user.UserSummaryResponse;
import com.artist_in.app.entity.User;

public final class UserMapper {
	private UserMapper() {
	}

	public static UserSummaryResponse toSummary(User user) {
		return UserSummaryResponse.builder().id(user.getId()).username(user.getUsername())
				.displayName(user.getDisplayName()).profilePhotoUrl(user.getProfilePhotoUrl())
				.accountType(user.getAccountType() != null ? user.getAccountType().name() : null).build();
	}
}