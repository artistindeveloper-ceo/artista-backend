package com.artist_in.app.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CountryDto {
	private Long id;
	private String name;
	private Long stateId;
	private String stateName;
	private Long countryId;
	private String countryName;
}
