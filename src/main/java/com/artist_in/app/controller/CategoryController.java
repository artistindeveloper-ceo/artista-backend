package com.artist_in.app.controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.instument.CategoryResponseDTO;
import com.artist_in.app.service.CategoryService;

import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
	private final CategoryService categoryService;

//	@GetMapping
//	public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
//		log.info("GET /categories request received");
//		List<CategoryResponseDTO> categories = categoryService.getAllActiveCategories();
//		log.info("GET /categories completed. Returned {} categories.", categories.size());
//		return ResponseEntity.ok(categories);
//
//	}
}