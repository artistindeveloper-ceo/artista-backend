package com.artist_in.app.dto.user;

import java.time.Instant;
import java.util.Set;

import com.artist_in.app.enums.InstrumentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
	private Long id;
	private String username;
	private String email;
	private String displayName;
	private String bio;
	private String profilePhotoUrl;
	private String coverPhotoUrl;
	private String location;
	private String websiteUrl;
	private InstrumentType primaryInstrument;
	private Set<InstrumentType> instruments;
	private String genres;
	private boolean isPrivate;
	private long followerCount;
	private long followingCount;
	private long postCount;

	/**
	 * Relationship of the currently authenticated viewer to this profile. Null if
	 * viewing own profile.
	 */
	private Boolean isFollowedByViewer;
	private Boolean hasPendingFollowRequestFromViewer;

	private Instant createdAt;
}
