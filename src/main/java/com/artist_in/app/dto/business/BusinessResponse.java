package com.artist_in.app.dto.business;

import com.artist_in.app.entity.Business;
import com.artist_in.app.entity.User;
import com.artist_in.app.entity.location.City;

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
	private String countryName;
	private String contactEmail;
	private String contactPhone;
	private String profilePhotoUrl;
	private String coverPhotoUrl;
	private boolean isVerified;
	private Double avgRating;
	private Integer ratingCount;
	private java.util.Map<String, Object> details;
	private long followerCount;
	private long followingCount; //
	private boolean isFollowedByViewer;

	public static BusinessResponse from(Business business) {
		return from(business, 0L, 0L, false);
	}

	public static BusinessResponse from(Business business, long followerCount, long followingCount,
			boolean isFollowedByViewer) {
		User owner = business.getOwner();
		City city = owner.getCity();

		String countryName = null;
		if (city != null && city.getState() != null && city.getState().getCountry() != null) {
			countryName = city.getState().getCountry().getName();
		}

		String businessType = business.getBusinessCategory() != null ? business.getBusinessCategory().getCode() : null;
		return BusinessResponse.builder().id(business.getId()).businessType(businessType).name(business.getName())
				.description(business.getDescription()).cityId(city != null ? city.getId() : null)
				.cityName(city != null ? city.getName() : null).countryName(countryName) // NAYA
				.contactEmail(owner.getEmail()).contactPhone(owner.getMobileNumber())
				.profilePhotoUrl(owner.getProfilePhotoUrl()).coverPhotoUrl(owner.getCoverPhotoUrl())
				.isVerified(owner.isVerified()).avgRating(business.getAvgRating())
				.ratingCount(business.getRatingCount()).details(business.getDetails()).followerCount(followerCount)
				.followingCount(followingCount).isFollowedByViewer(isFollowedByViewer).build();
	}
}