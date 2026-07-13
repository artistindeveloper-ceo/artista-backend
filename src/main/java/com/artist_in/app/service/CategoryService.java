package com.artist_in.app.service;

import java.util.List;
import com.artist_in.app.dto.instument.CategoryResponseDTO;

public interface CategoryService {
	List<CategoryResponseDTO> getAllActiveCategories();
}
