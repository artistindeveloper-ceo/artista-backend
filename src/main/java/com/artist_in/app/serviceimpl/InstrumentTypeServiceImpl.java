package com.artist_in.app.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.instument.InstrumentTypeResponseDTO;
import com.artist_in.app.instument.entity.InstrumentType;
import com.artist_in.app.instument.repository.InstrumentTypeRepository;
import com.artist_in.app.service.InstrumentTypeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentTypeServiceImpl implements InstrumentTypeService {
	private final InstrumentTypeRepository instrumentTypeRepository;

	@Override
	@Transactional(readOnly = true)
	public List<InstrumentTypeResponseDTO> getTypesByCategory(Integer categoryId) {
		log.debug("Fetching instrument types from database for categoryId={}", categoryId);
		List<InstrumentType> types = instrumentTypeRepository.findByCategoryIdAndIsActiveTrueOrderByNameAsc(categoryId);
		log.debug("Found {} instrument types for categoryId={}", types.size(), categoryId);
		return types.stream()
				.map(t -> InstrumentTypeResponseDTO.builder().id(t.getId()).name(t.getName())
						.description(t.getDescription()).icon(t.getIcon()).categoryId(categoryId).build())
				.collect(Collectors.toList());
	}
}