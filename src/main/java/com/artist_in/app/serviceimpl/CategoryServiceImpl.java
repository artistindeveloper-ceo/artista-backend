package com.artist_in.app.serviceimpl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.business.CategoryResponse;
import com.artist_in.app.entity.Professional.ProfileCategory;
import com.artist_in.app.entity.business.BusinessCategory;
import com.artist_in.app.instument.repository.CategoryRepository;
import com.artist_in.app.repository.BusinessCategoryRepository;
import com.artist_in.app.repository.ProfileCategoryRepository;
import com.artist_in.app.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Public lookup endpoints — Create Account screen (Flutter) ke Individual /
 * Business dropdowns yahan se list load karte hain, taaki DB me naya category
 * add karne par app side koi code change na karna pade.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

//	@Override

//	@Transactional(readOnly = true)
//	public List<CategoryResponseDTO> getAllActiveCategories() {
//
//		log.info("Fetching all active categories.");
//
//		List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
//
//		log.info("Found {} active categories.", categories.size());
//
//		return categories.stream().map(c -> CategoryResponseDTO.builder().id(c.getId()).name(c.getName())
//				.description(c.getDescription()).icon(c.getIcon()).build()).collect(Collectors.toList());
//	}

	private final ProfileCategoryRepository profileCategoryRepository;
	private final BusinessCategoryRepository businessCategoryRepository;

	// Individual account type ke liye — MUSICIAN, PHOTOGRAPHER, EVENT_MANAGER...
	@GetMapping("/profile-categories")
	public ResponseEntity<List<CategoryResponse>> getProfileCategories() {
		List<ProfileCategory> categories = profileCategoryRepository
				.findByIsActiveTrueOrderBySortOrderAscCategoryNameAsc();

		List<CategoryResponse> response = categories.stream()
				.map(c -> CategoryResponse.builder().id(c.getId()).code(c.getCode()).displayName(c.getCategoryName())
						.description(c.getDescription()).iconUrl(c.getIconUrl()).sortOrder(c.getSortOrder()).build())
				.toList();

		log.debug("Fetched {} active profile categories", response.size());
		return ResponseEntity.ok(response);
	}

	// Business account type ke liye — SHOP, ACADEMY, SCHOOL, INSTITUTE...
	@GetMapping("/business-categories")
	public ResponseEntity<List<CategoryResponse>> getBusinessCategories() {
		List<BusinessCategory> categories = businessCategoryRepository
				.findByIsActiveTrueOrderBySortOrderAscDisplayNameAsc();

		List<CategoryResponse> response = categories.stream()
				.map(c -> CategoryResponse.builder().id(c.getId()).code(c.getCode()).displayName(c.getDisplayName())
						.description(c.getDescription()).iconUrl(c.getIconUrl()).sortOrder(c.getSortOrder()).build())
				.toList();

		log.debug("Fetched {} active business categories", response.size());
		return ResponseEntity.ok(response);
	}
}