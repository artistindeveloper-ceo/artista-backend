package com.artist_in.app.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.instument.CategoryResponseDTO;
import com.artist_in.app.instument.entity.Category;
import com.artist_in.app.instument.repository.CategoryRepository;
import com.artist_in.app.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;

	@Override

	@Transactional(readOnly = true)
	public List<CategoryResponseDTO> getAllActiveCategories() {

		log.info("Fetching all active categories.");

		List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();

		log.info("Found {} active categories.", categories.size());

		return categories.stream().map(c -> CategoryResponseDTO.builder().id(c.getId()).name(c.getName())
				.description(c.getDescription()).icon(c.getIcon()).build()).collect(Collectors.toList());
	}
}