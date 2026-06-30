package com.artist_in.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.user.DiscoverUserDto;
import com.artist_in.app.security.UserPrincipal;
import com.artist_in.app.service.CommunityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/community/users")
@RequiredArgsConstructor
public class CommunityController {

	private final CommunityService discoverService;

	@GetMapping("/discover")
	public ResponseEntity<Page<DiscoverUserDto>> discoverUsers(@AuthenticationPrincipal UserPrincipal currentUser,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		Page<DiscoverUserDto> result = discoverService.getDiscoverUsers(currentUser.getId(), page, size);

		return ResponseEntity.ok(result);
	}
}
