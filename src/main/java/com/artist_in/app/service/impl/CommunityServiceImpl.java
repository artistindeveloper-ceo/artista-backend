package com.artist_in.app.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.artist_in.app.dto.user.DiscoverUserDto;
import com.artist_in.app.repository.CommunityRepository;
import com.artist_in.app.service.CommunityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

	private final CommunityRepository discoverRepository;

	/**
	 * Returns paginated list of users the current user has NOT followed yet, newest
	 * members first (Instagram/Facebook "People You May Know" style).
	 *
	 * @param currentUserId ID of the logged-in user (from JWT)
	 * @param page          page number (0-based)
	 * @param size          users per page (default 20)
	 */
	public Page<DiscoverUserDto> getDiscoverUsers(Long currentUserId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return discoverRepository.findDiscoverUsers(currentUserId, pageable).map(DiscoverUserDto::from);
	}
}
