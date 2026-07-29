package com.artist_in.app.serviceimpl;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.business.BusinessCreateRequest;
import com.artist_in.app.dto.business.BusinessUpdateRequest;
import com.artist_in.app.entity.Business;
import com.artist_in.app.entity.User;
import com.artist_in.app.entity.location.City;
import com.artist_in.app.repository.BusinessRepository;
import com.artist_in.app.repository.UserRepository;
import com.artist_in.app.repository.location.CityRepository;
import com.artist_in.app.service.BusinessService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

	private final BusinessRepository businessRepository;
	private final UserRepository userRepository;
	private final CityRepository cityRepository;

	@Transactional
	public Business create(Long ownerUserId, BusinessCreateRequest req) {
		User owner = userRepository.findById(ownerUserId)
				.orElseThrow(() -> new EntityNotFoundException("User not found: " + ownerUserId));

		City city = req.getCityId() != null ? cityRepository.findById(req.getCityId())
				.orElseThrow(() -> new EntityNotFoundException("City not found: " + req.getCityId())) : null;

		// id manually set NAHI karna — @MapsId khud owner.getId() se copy kar lega
		// jab persist() call hoga (id null hone ki wajah se Spring Data
		// persist() choose karega, merge() nahi)
		Business business = Business.builder().owner(owner).name(req.getName()).description(req.getDescription())
				.city(city).details(req.getDetails()).build();

		return businessRepository.save(business);
	}

	@Transactional(readOnly = true)
	public Business getById(Long id) {
		return businessRepository.findActiveByIdWithCity(id)
				.orElseThrow(() -> new EntityNotFoundException("Business not found: " + id));
	}

	@Transactional
	public Business update(Long businessId, Long requestingUserId, BusinessUpdateRequest req) {
		Business business = getById(businessId);
		requireOwner(business, requestingUserId);

		if (req.getName() != null)
			business.setName(req.getName());
		if (req.getDescription() != null)
			business.setDescription(req.getDescription());
		if (req.getDetails() != null)
			business.getDetails().putAll(req.getDetails());
		if (req.getCityId() != null) {
			City city = cityRepository.findById(req.getCityId())
					.orElseThrow(() -> new EntityNotFoundException("City not found: " + req.getCityId()));
			business.setCity(city);
		}
		// dirty checking within @Transactional handles the UPDATE — no explicit save()
		// needed
		return business;
	}

	@Transactional
	public void softDelete(Long businessId, Long requestingUserId) {
		Business business = getById(businessId);
		requireOwner(business, requestingUserId);
		business.setActive(false);
	}

	@Transactional(readOnly = true)
	public Long getPrimaryOwnerUserId(Long businessId) {
		return getById(businessId).getOwner().getId();
	}

	private void requireOwner(Business business, Long userId) {
		if (!business.getOwner().getId().equals(userId)) {
			throw new AccessDeniedException("Only the owner can perform this action");
		}
	}

}