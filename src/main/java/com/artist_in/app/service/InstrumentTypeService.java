package com.artist_in.app.service;

import java.util.List;

import com.artist_in.app.dto.instument.InstrumentTypeResponseDTO;

public interface InstrumentTypeService {
	List<InstrumentTypeResponseDTO> getTypesByCategory(Integer categoryId);
}
