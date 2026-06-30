package com.artist_in.app.dto.user;

import java.util.Set;

import com.artist_in.app.enums.InstrumentType;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

	@Size(max = 100, message = "Display name must be at most 100 characters.")
	private String displayName;

	@Size(max = 500, message = "Bio must be at most 500 characters.")
	private String bio;

	@Size(max = 150, message = "Location must be at most 150 characters.")
	private String location;

	private String websiteUrl;

	private InstrumentType primaryInstrument;

	private Set<InstrumentType> instruments;

	@Size(max = 300, message = "Genres must be at most 300 characters.")
	private String genres;

	private Boolean isPrivate;
}
