package com.artist_in.app.dto.instument;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentStats {
	// Usage statistics
	private Long totalUsers;
	private Long totalUserInstruments;
	private Long totalReviews;

	// Rating statistics
	private Double averageRating;
	private Integer ratingCount1;
	private Integer ratingCount2;
	private Integer ratingCount3;
	private Integer ratingCount4;
	private Integer ratingCount5;

	// Price statistics
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
	private BigDecimal averagePrice;
	private BigDecimal medianPrice;

	// Popularity metrics
	private Long viewCount;
	private Long saveCount;
	private Long shareCount;

	// User distribution
	private Long beginnerCount;
	private Long intermediateCount;
	private Long advancedCount;
	private Long professionalCount;

	// Time-based statistics
	private Integer yearsInProduction;
	private Long instrumentsSoldLastYear;
	private Long instrumentsSoldLastMonth;

	// Comparison metrics
	private Double popularityScore;
	private Double valueForMoneyScore;

	// Nested stats
	private BrandStats brandStats;
	private CategoryStats categoryStats;

	@Data
	@Builder
	public static class BrandStats {
		private Long totalInstrumentsInBrand;
		private Integer brandRank;
		private Double brandMarketShare;
	}

	@Data
	@Builder
	public static class CategoryStats {
		private Long totalInstrumentsInCategory;
		private Integer categoryRank;
		private Double categoryMarketShare;
	}
}
