package com.artist_in.app.validator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class ProfileValidatorFactory {
	private final Map<String, ProfileValidator> validatorMap;

	public ProfileValidatorFactory(List<ProfileValidator> validators) {
		this.validatorMap = validators.stream().collect(Collectors.toMap(v -> v.getRoleType().toUpperCase(), v -> v));
	}

	public ProfileValidator getValidator(String type) {
		ProfileValidator validator = validatorMap.get(type.toUpperCase());
		if (validator == null) {
			throw new IllegalArgumentException("Unsupported professional type: " + type);
		}
		return validator;
	}
}
