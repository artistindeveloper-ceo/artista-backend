package com.artist_in.app.dto.instument;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInstrumentRequestDTO {

	private Long userId;

	@NotNull(message = "Instrument ID is required")
	private Integer instrumentId;

	private Boolean isPrimary = false;
	private String proficiencyLevel;
	private BigDecimal yearsExperience;
	private String serialNumber;
	private LocalDate purchaseDate;
	private String notes;
	private JsonNode customDetails;
}
