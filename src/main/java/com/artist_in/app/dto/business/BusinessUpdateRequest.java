package com.artist_in.app.dto.business;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUpdateRequest {

	private String name;
	private String description;
	private Long cityId;
	private String contactEmail;
	private String contactPhone;
	private String coverPhotoUrl;
	private Map<String, Object> details;
}
