package com.artist_in.app.validator;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.artist_in.app.dto.profile.ProfileDetailKeys;

@Component("SOUND_ENGINEER")
public class SoundEngineerValidator implements ProfileValidator {
	@Override
	public void validate(Map<String, Object> details) {
		if (details == null || !details.containsKey(ProfileDetailKeys.EQUIPMENT)) {
			throw new IllegalArgumentException("Equipment details are mandatory for Sound Engineers");
		}
	}

	@Override
	public String getRoleType() {
		return "SOUND_ENGINEER";
	}

}
