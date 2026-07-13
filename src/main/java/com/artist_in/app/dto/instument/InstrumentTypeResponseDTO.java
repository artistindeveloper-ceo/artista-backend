package com.artist_in.app.dto.instument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentTypeResponseDTO {
	private Integer id;
	private String name;
	private String description;
	private String icon;
	private Integer categoryId;
}