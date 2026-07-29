package com.artist_in.app.dto.business;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
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
public class BusinessCreateRequest {

	@NotBlank
	private String name;

	private String description;

	private Long cityId;

	private Map<String, Object> details;
}