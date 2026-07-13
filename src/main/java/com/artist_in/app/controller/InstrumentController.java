package com.artist_in.app.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.instument.InstrumentResponseDTO;
import com.artist_in.app.dto.instument.UserInstrumentRequestDTO;
import com.artist_in.app.instument.entity.UserInstrument;
import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.service.InstrumentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {
	private final InstrumentService instrumentService;

	@GetMapping("/{id}")
	public ResponseEntity<InstrumentResponseDTO> getInstrument(@PathVariable Integer id) {
		return ResponseEntity.ok(instrumentService.getInstrumentById(id));
	}

	@GetMapping
	public ResponseEntity<Page<InstrumentResponseDTO>> getAllInstruments(
			@PageableDefault(size = 20, sort = "id") Pageable pageable) {
		return ResponseEntity.ok(instrumentService.getAllInstruments(pageable));
	}

	@GetMapping("/search")
	public ResponseEntity<List<InstrumentResponseDTO>> searchInstruments(
			@RequestParam(required = false) String brandName, @RequestParam(required = false) String categoryName,
			@RequestParam(required = false) String typeName, @RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice) {
		return ResponseEntity
				.ok(instrumentService.searchInstruments(brandName, categoryName, typeName, minPrice, maxPrice));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<InstrumentResponseDTO>> getUserInstruments(@PathVariable Integer userId) {
		return ResponseEntity.ok(instrumentService.getUserInstruments(userId));
	}

	@PostMapping("/user/add")
	public ResponseEntity<UserInstrument> addInstrumentToUser(@Valid @RequestBody UserInstrumentRequestDTO request,
			Authentication authentication) {
		UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
		Long userId = principal.getUser().getId(); // ya principal.getId() — file dekh ke confirm karunga
		request.setUserId(userId);
		return ResponseEntity.ok(instrumentService.addInstrumentToUser(request));
	}

	@GetMapping("/by-type")
	public ResponseEntity<Page<InstrumentResponseDTO>> getInstrumentsByType(@RequestParam Integer typeId,
			@PageableDefault(size = 20, sort = "model") Pageable pageable) {
		return ResponseEntity.ok(instrumentService.getInstrumentsByType(typeId, pageable));
	}
}
