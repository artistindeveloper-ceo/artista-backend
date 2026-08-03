package com.artist_in.app.dto.user;

import java.time.Instant;

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
	private String genres;

	// Legacy field — INDIVIDUAL ke liye ProfileCategory.code (MUSICIAN,
	// PHOTOGRAPHER...) yahan continue rehta hai backward compatibility ke liye.
	// BUSINESS account ke liye ye ab null rahega — categoryCode use karo.
	private String roleType;

	// INDIVIDUAL ya BUSINESS — Flutter isi field se reliably decide karega,
	// koi guessing/set-matching ki zaroorat nahi.
	private String accountType;

	// Dono account type ke liye common — INDIVIDUAL ke liye ProfileCategory.code,
	// BUSINESS ke liye BusinessCategory.code (SHOP, ACADEMY, SCHOOL, INSTITUTE)
	private String categoryCode;
	private String categoryDisplayName;

	// BUSINESS account ke liye hi set hota hai
	private String businessName;

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