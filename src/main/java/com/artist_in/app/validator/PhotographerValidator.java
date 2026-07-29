package com.artist_in.app.validator;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.artist_in.app.dto.profile.ProfileDetailKeys;

@Component("PHOTOGRAPHER")
public class PhotographerValidator implements ProfileValidator {
	@Override
	public void validate(Map<String, Object> details) {
		if (details == null || !details.containsKey(ProfileDetailKeys.CAMERA_GEAR)) {
			throw new IllegalArgumentException("Camera gear details are mandatory for Photographers");
		}
	}

	@Override
	public String getRoleType() {
		return "PHOTOGRAPHER";
	}
}