package com.artist_in.app.service;

import com.artist_in.app.dto.business.BusinessCreateRequest;
import com.artist_in.app.dto.business.BusinessUpdateRequest;
import com.artist_in.app.entity.Business;

import jakarta.validation.Valid;

public interface BusinessService {

	Business create(Long id, @Valid BusinessCreateRequest req);

	Business getById(Long id);

	Business update(Long id, Long id2, BusinessUpdateRequest req);

	void softDelete(Long id, Long id2);

	Long getPrimaryOwnerUserId(Long id);

}
