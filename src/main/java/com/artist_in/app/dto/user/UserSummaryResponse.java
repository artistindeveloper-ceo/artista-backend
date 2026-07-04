package com.artist_in.app.dto.user;

import com.artist_in.app.enums.InstrumentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public class UserSummaryResponse {
		private Long id;
		private String username;
		private String displayName;
		private String profilePhotoUrl;
		private InstrumentType primaryInstrument;
		private boolean isFollowing;
		private boolean hasPendingFollowRequest;
	}
