package com.artist_in.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.artist_in.app.entity.Profile;

public interface ProfileService {

	Profile createOrUpdateProfile(Long userId, String professionalType, Long cityId, String mobileNumber,
			Map<String, Object> details);

	Profile getProfileById(Long userId);

	List<Profile> searchProfiles(String city, String professionalType);

	Page<Profile> searchTopRatedProfiles(String professionalType, String city, String state, String country,
			String instrument, Pageable pageable);

	Profile addRating(Long profileId, Long ratedByUserId, Integer stars, String comment);

	void deleteProfile(Long userId);

}