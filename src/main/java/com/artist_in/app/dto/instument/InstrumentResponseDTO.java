package com.artist_in.app.dto.instument;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentResponseDTO {
	private Integer id;
	private String model;
	private Integer modelYear;
	private String description;
	private String imageUrl;
	private String thumbnailUrl;
	private JsonNode specifications;
	private BigDecimal price;
	private String currency;

	// Nested objects
	private BrandDTO brand;
	private InstrumentTypeDTO instrumentType;
	private CategoryDTO category;

	// Aggregated data
	private List<MediaDTO> media;
	private Double avgRating;
	private Integer totalReviews;
	private List<ReviewDTO> recentReviews;

	// User-specific
	private Boolean isUserInstrument;
	private UserInstrumentDTO userInstrumentDetails;

	@Data
	@Builder
	public static class BrandDTO {
		private Integer id;
		private String name;
		private String logoUrl;
		private String country;
	}

	@Data
	@Builder
	public static class InstrumentTypeDTO {
		private Integer id;
		private String name;
		private String icon;
	}

	@Data
	@Builder
	public static class CategoryDTO {
		private Integer id;
		private String name;
		private String icon;
	}

	@Data
	@Builder
	public static class MediaDTO {
		private Integer id;
		private String url;
		private String mediaType;
		private Boolean isPrimary;
		private String caption;
	}

	@Data
	@Builder
	public static class ReviewDTO {
		private Integer id;
		private String username;
		private Integer rating;
		private String reviewText;
		private String createdAt;
	}

	@Data
	@Builder
	public static class UserInstrumentDTO {
		private Integer id;
		private Boolean isPrimary;
		private String proficiencyLevel;
		private BigDecimal yearsExperience;
		private String purchaseDate;
		private JsonNode customDetails;
	}
}
