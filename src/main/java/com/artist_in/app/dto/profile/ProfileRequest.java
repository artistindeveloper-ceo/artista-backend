package com.artist_in.app.dto.profile;

import java.util.Map;

import lombok.Data;

@Data
public class ProfileRequest {
//	@NotBlank
	private String professionalType; // "EVENT_MANAGER"

//	@NotBlank
	private String displayName;

	private String city;

//	@NotNull
	private Map<String, Object> details; // role-specific data
}
