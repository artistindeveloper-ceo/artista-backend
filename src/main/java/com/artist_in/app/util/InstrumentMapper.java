package com.artist_in.app.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.artist_in.app.dto.instument.InstrumentResponseDTO;
import com.artist_in.app.instument.entity.Instrument;
import com.artist_in.app.instument.entity.InstrumentMedia;
import com.artist_in.app.instument.entity.InstrumentReview;

@Component
public class InstrumentMapper {
	public InstrumentResponseDTO toResponseDTO(Instrument instrument) {
		if (instrument == null) {
			return null;
		}

		return InstrumentResponseDTO.builder().id(instrument.getId()).model(instrument.getModel())
				.modelYear(instrument.getModelYear()).description(instrument.getDescription())
				.imageUrl(instrument.getImageUrl()).thumbnailUrl(instrument.getThumbnailUrl())
				.specifications(instrument.getSpecifications()).price(instrument.getPrice())
				.currency(instrument.getCurrency())

				// Brand
				.brand(InstrumentResponseDTO.BrandDTO.builder().id(instrument.getBrand().getId())
						.name(instrument.getBrand().getName()).logoUrl(instrument.getBrand().getLogoUrl())
						.country(instrument.getBrand().getCountry()).build())

				// Instrument Type
				.instrumentType(InstrumentResponseDTO.InstrumentTypeDTO.builder()
						.id(instrument.getInstrumentType().getId()).name(instrument.getInstrumentType().getName())
						.icon(instrument.getInstrumentType().getIcon()).build())

				// Category
				.category(InstrumentResponseDTO.CategoryDTO.builder()
						.id(instrument.getInstrumentType().getCategory().getId())
						.name(instrument.getInstrumentType().getCategory().getName())
						.icon(instrument.getInstrumentType().getCategory().getIcon()).build())

				// Media
				.media(instrument.getMedia().stream().map(this::toMediaDTO).collect(Collectors.toList()))

				// Reviews
				.recentReviews(
						instrument.getReviews().stream().limit(5).map(this::toReviewDTO).collect(Collectors.toList()))

				.avgRating(calculateAverageRating(instrument.getReviews())).totalReviews(instrument.getReviews().size())
				.build();
	}

	private InstrumentResponseDTO.MediaDTO toMediaDTO(InstrumentMedia media) {
		return InstrumentResponseDTO.MediaDTO.builder().id(media.getId()).url(media.getUrl())
				.mediaType(media.getMediaType().name()).isPrimary(media.getIsPrimary()).caption(media.getCaption())
				.build();
	}

	private InstrumentResponseDTO.ReviewDTO toReviewDTO(InstrumentReview review) {
		return InstrumentResponseDTO.ReviewDTO.builder().id(review.getId()).username(review.getUser().getUsername())
				.rating(review.getRating()).reviewText(review.getReviewText())
				.createdAt(review.getCreatedAt().toString()).build();
	}

	private Double calculateAverageRating(List<InstrumentReview> reviews) {
		if (reviews == null || reviews.isEmpty()) {
			return null;
		}
		return reviews.stream().mapToInt(InstrumentReview::getRating).average().orElse(0.0);
	}
}
