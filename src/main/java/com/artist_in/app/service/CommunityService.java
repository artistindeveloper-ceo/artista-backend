package com.artist_in.app.service;

import org.springframework.data.domain.Page;

import com.artist_in.app.dto.user.DiscoverUserDto;

public interface CommunityService {

	Page<DiscoverUserDto> getDiscoverUsers(Long id, int page, int size);

}
