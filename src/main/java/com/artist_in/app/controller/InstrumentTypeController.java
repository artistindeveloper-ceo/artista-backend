package com.artist_in.app.controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.artist_in.app.dto.instument.InstrumentTypeResponseDTO;
import com.artist_in.app.service.InstrumentTypeService;
import lombok.RequiredArgsConstructor;
@Slf4j
@RestController
@RequestMapping("/api/instrument-types")
@RequiredArgsConstructor
public class InstrumentTypeController {
	private final InstrumentTypeService instrumentTypeService;

	@GetMapping
	public ResponseEntity<List<InstrumentTypeResponseDTO>> getTypesByCategory(@RequestParam Integer categoryId) {
		log.debug("Fetching instrument types for categoryId={}", categoryId);
		return ResponseEntity.ok(instrumentTypeService.getTypesByCategory(categoryId));
	}
}