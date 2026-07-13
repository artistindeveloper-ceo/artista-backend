package com.artist_in.app.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.artist_in.app.dto.instument.InstrumentResponseDTO;
import com.artist_in.app.dto.instument.UserInstrumentRequestDTO;
import com.artist_in.app.instument.entity.UserInstrument;

import jakarta.validation.Valid;

public interface InstrumentService {

	InstrumentResponseDTO getInstrumentById(Integer id);

	Page<InstrumentResponseDTO> getAllInstruments(Pageable pageable);

	List<InstrumentResponseDTO> searchInstruments(String brandName, String categoryName, String typeName,
			BigDecimal minPrice, BigDecimal maxPrice);

	List<InstrumentResponseDTO> getUserInstruments(Integer userId);

	UserInstrument addInstrumentToUser(@Valid UserInstrumentRequestDTO request);

	Page<InstrumentResponseDTO> getInstrumentsByType(Integer typeId, Pageable pageable);
}
