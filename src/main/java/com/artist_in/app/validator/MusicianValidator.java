package com.artist_in.app.validator;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.artist_in.app.dto.profile.ProfileDetailKeys;

@Component("MUSICIAN")
public class MusicianValidator implements ProfileValidator {
	@Override
	public void validate(Map<String, Object> details) {
		if (details == null || !details.containsKey(ProfileDetailKeys.PRIMARY_INSTRUMENT)) {
			throw new IllegalArgumentException("Primary instrument is mandatory for Musicians");
		}
	}

	@Override
	public String getRoleType() {
		return "MUSICIAN";
	}
}