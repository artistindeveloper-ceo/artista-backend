package com.artist_in.app.validator;

import java.util.Map;

public interface ProfileValidator {

	void validate(Map<String, Object> details);

	String getRoleType();
}
