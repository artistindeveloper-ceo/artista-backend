package com.artist_in.app.dto.business;

import com.artist_in.app.entity.Business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessResponse {

	private Long id;
	private String businessType;
	private String name;
	private String description;
	private Long cityId;
	private String cityName;
	private String contactEmail;
	private String contactPhone;
	private String profilePhotoUrl;
	private String coverPhotoUrl;
	private boolean isVerified;
	private Double avgRating;
	private Integer ratingCount;
	private java.util.Map<String, Object> details;
	private long followerCount;
	private boolean isFollowedByViewer;

	public static BusinessResponse from(Business business) {
		return from(business, 0L, false);
	}

	public static BusinessResponse from(Business business, long followerCount, boolean isFollowedByViewer) {
		return BusinessResponse.builder().id(business.getId()).businessType(business.getOwner().getRoleType())
				.name(business.getName()).description(business.getDescription())
				.cityId(business.getCity() != null ? business.getCity().getId() : null)
				.cityName(business.getCity() != null ? business.getCity().getName() : null)
				.contactEmail(business.getOwner().getEmail()).contactPhone(business.getOwner().getMobileNumber())
				.profilePhotoUrl(business.getOwner().getProfilePhotoUrl())
				.coverPhotoUrl(business.getOwner().getCoverPhotoUrl()).isVerified(business.getOwner().isVerified())
				.avgRating(business.getAvgRating()).ratingCount(business.getRatingCount())
				.details(business.getDetails()).followerCount(followerCount).isFollowedByViewer(isFollowedByViewer)
				.build();
	}
}