package com.artist_in.app.serviceimpl;

import java.util.HashMap;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.business.BusinessCreateRequest;
import com.artist_in.app.dto.business.BusinessUpdateRequest;
import com.artist_in.app.entity.Business;
import com.artist_in.app.entity.User;
import com.artist_in.app.entity.business.BusinessCategory;
import com.artist_in.app.entity.location.City;
import com.artist_in.app.repository.BusinessCategoryRepository;
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
	private final BusinessCategoryRepository businessCategoryRepository;

	@Transactional
	public Business create(Long ownerUserId, BusinessCreateRequest req) {
		User owner = userRepository.findById(ownerUserId)
				.orElseThrow(() -> new EntityNotFoundException("User not found: " + ownerUserId));

		City city = null;
		if (req.getCityId() != null) {
			city = cityRepository.findById(req.getCityId())
					.orElseThrow(() -> new EntityNotFoundException("City not found: " + req.getCityId()));
			owner.setCity(city);
		}

		// businessCategoryCode (SHOP, ACADEMY, SCHOOL, INSTITUTE...) ko DB se
		// resolve karke Business.businessCategory pe set karna zaroori hai —
		// pehle ye step missing tha isliye business_category_id hamesha null
		// save ho raha tha.
		BusinessCategory businessCategory = resolveBusinessCategory(req.getBusinessCategoryCode());

		Business business = Business.builder().owner(owner).name(req.getName()).description(req.getDescription())
				.businessCategory(businessCategory).details(req.getDetails()).build();
		return businessRepository.save(business);
	}

	private BusinessCategory resolveBusinessCategory(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("businessType is required for a business account");
		}
		return businessCategoryRepository.findByCodeIgnoreCaseAndIsActiveTrue(code)
				.orElseThrow(() -> new IllegalArgumentException("Invalid businessType: " + code));
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
		if (req.getName() != null) {
			business.setName(req.getName().trim());
		}
		if (req.getDescription() != null) {
			business.setDescription(req.getDescription().trim());
		}
		if (req.getDetails() != null) {
			if (business.getDetails() == null) {
				business.setDetails(new HashMap<>());
			}
			business.getDetails().putAll(req.getDetails());
		}
		if (req.getCityId() != null) {
			City city = cityRepository.findById(req.getCityId())
					.orElseThrow(() -> new EntityNotFoundException("City not found: " + req.getCityId()));
			business.getOwner().setCity(city);
		}
		if (req.getContactPhone() != null) { // NAYA
			business.getOwner().setMobileNumber(req.getContactPhone().trim());
		}
		if (req.getContactEmail() != null) { // NAYA
			business.getOwner().setEmail(req.getContactEmail().trim());
		}
		if (req.getCoverPhotoUrl() != null) { // NAYA — DTO me hai but ye bhi handle nahi ho raha tha
			business.getOwner().setCoverPhotoUrl(req.getCoverPhotoUrl());
		}
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