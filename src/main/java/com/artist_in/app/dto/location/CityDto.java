package com.artist_in.app.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * CityDto carries state + country name flattened in, so the Flutter app doesn't
 * need extra calls just to show "Indore, Madhya Pradesh, India" after fetching
 * a profile by cityId.
 */
@Data
@AllArgsConstructor
public class CityDto {
	private Long id;
	private String name;
	private Long stateId;
	private String stateName;
	private Long countryId;
	private String countryName;
}
