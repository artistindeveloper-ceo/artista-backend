package com.artist_in.app.serviceimpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.instument.InstrumentResponseDTO;
import com.artist_in.app.dto.instument.InstrumentStats;
import com.artist_in.app.dto.instument.UserInstrumentRequestDTO;
import com.artist_in.app.entity.User;
import com.artist_in.app.instument.entity.Instrument;
import com.artist_in.app.instument.entity.UserInstrument;
import com.artist_in.app.repository.InstrumentRepository;
import com.artist_in.app.repository.UserInstrumentRepository;
import com.artist_in.app.service.InstrumentService;
import com.artist_in.app.util.InstrumentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentServiceImpl implements InstrumentService {
	private final InstrumentRepository instrumentRepository;
	private final UserInstrumentRepository userInstrumentRepository;
	private final InstrumentMapper instrumentMapper;

	@Cacheable(value = "instruments", key = "#id")
	@Transactional(readOnly = true)
	public InstrumentResponseDTO getInstrumentById(Integer id) {
		log.debug("Fetching instrument from database: {}", id);
		Instrument instrument = instrumentRepository.findInstrumentWithDetails(id)
				.orElseThrow(() -> new RuntimeException("Instrument not found"));
		return instrumentMapper.toResponseDTO(instrument);
	}

	@Cacheable(value = "instruments", key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
	@Transactional(readOnly = true)
	public Page<InstrumentResponseDTO> getAllInstruments(Pageable pageable) {
		Page<Instrument> instruments = instrumentRepository.findAll(pageable);
		return instruments.map(instrumentMapper::toResponseDTO);
	}

	@Cacheable(value = "instruments", key = "'filter_' + #brandName + '_' + #categoryName + '_' + #typeName")
	@Transactional(readOnly = true)
	public List<InstrumentResponseDTO> searchInstruments(String brandName, String categoryName, String typeName,
			BigDecimal minPrice, BigDecimal maxPrice) {
		Page<Instrument> instruments = instrumentRepository.findInstrumentsWithFilters(brandName, categoryName,
				typeName, minPrice, maxPrice, Pageable.unpaged());
		return instruments.getContent().stream().map(instrumentMapper::toResponseDTO).collect(Collectors.toList());
	}

	@CacheEvict(value = "instruments", allEntries = true)
	@Transactional
	public InstrumentResponseDTO addInstrument(Instrument instrument) {
		Instrument saved = instrumentRepository.save(instrument);
		return instrumentMapper.toResponseDTO(saved);
	}

	@CacheEvict(value = { "instruments", "userInstruments" }, allEntries = true)
	@Transactional
	public UserInstrument addInstrumentToUser(UserInstrumentRequestDTO request) {
		if (Boolean.TRUE.equals(request.getIsPrimary())) {
			userInstrumentRepository.clearPrimaryInstrument(request.getUserId());
		}

		UserInstrument.ProficiencyLevel proficiencyLevel = null;
		if (request.getProficiencyLevel() != null && !request.getProficiencyLevel().isBlank()) {
			proficiencyLevel = UserInstrument.ProficiencyLevel
					.valueOf(request.getProficiencyLevel().trim().toUpperCase());
		}

		UserInstrument userInstrument = UserInstrument.builder().user(User.builder().id(request.getUserId()).build())
				.instrument(Instrument.builder().id(request.getInstrumentId()).build())
				.isPrimary(request.getIsPrimary()).proficiencyLevel(proficiencyLevel)
				.yearsExperience(request.getYearsExperience()).serialNumber(request.getSerialNumber())
				.purchaseDate(request.getPurchaseDate()).notes(request.getNotes())
				.customDetails(request.getCustomDetails()).isActive(true).build();

		return userInstrumentRepository.save(userInstrument);
	}

	@Cacheable(value = "userInstruments", key = "#userId")
	@Transactional(readOnly = true)
	public List<InstrumentResponseDTO> getUserInstruments(Integer userId) {
		List<UserInstrument> userInstruments = userInstrumentRepository.findUserInstrumentsWithDetails(userId);

		return userInstruments.stream().map(ui -> {
			InstrumentResponseDTO dto = instrumentMapper.toResponseDTO(ui.getInstrument());
			dto.setIsUserInstrument(true);
			dto.setUserInstrumentDetails(
					InstrumentResponseDTO.UserInstrumentDTO.builder().id(ui.getId()).isPrimary(ui.getIsPrimary())
							.proficiencyLevel(ui.getProficiencyLevel() != null ? ui.getProficiencyLevel().name() : null)
							.purchaseDate(ui.getPurchaseDate() != null ? ui.getPurchaseDate().toString() : null)
							.customDetails(ui.getCustomDetails()).build());
			return dto;
		}).collect(Collectors.toList());
	}

	@Cacheable(value = "instrumentStats", key = "#instrumentId")
	public InstrumentStats getInstrumentStats(Integer instrumentId) {
		Long totalUsers = userInstrumentRepository.countUsersWithInstrument(instrumentId);
		// Calculate average rating from reviews...
		return InstrumentStats.builder().totalUsers(totalUsers).averageRating(4.5) // Example
				.build();
	}

	@Cacheable
	@Transactional(readOnly = true)
	public Page<InstrumentResponseDTO> getInstrumentsByType(Integer typeId, Pageable pageable) {
		Page<Instrument> instruments = instrumentRepository.findByInstrumentTypeIdAndIsActiveTrue(typeId, pageable);
		return instruments.map(instrumentMapper::toResponseDTO);
	}
}
