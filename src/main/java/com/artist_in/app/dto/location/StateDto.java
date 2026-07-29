package com.artist_in.app.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StateDto {

	private Long id;
	private String name;
	private Long countryId;

}
