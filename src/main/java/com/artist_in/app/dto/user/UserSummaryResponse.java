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

	// INDIVIDUAL ya BUSINESS — Flutter login/register response se seedha Session
	// me save karta hai, taaki app ko pata rahe kis profile screen pe route
	// karna hai (ProfileScreen vs BusinessProfileScreen), page reload ke bina.
	private String accountType;
}