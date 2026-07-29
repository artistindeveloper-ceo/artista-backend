package com.artist_in.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.artist_in.app.entity.Profile;
import com.artist_in.app.service.ProfileService;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
	private final ProfileService profileService;

	public ProfileController(ProfileService profileService) {
		this.profileService = profileService;
	}

	// 1. Create or Update Profile API (POST / PUT)
	// city/state/country query params replaced by a single cityId — the
	// user now picks a City (autocomplete on Flutter side) instead of typing
	// free text, and state/country are derived from that City on read.
	@PostMapping("/{userId}")
	public ResponseEntity<Profile> saveProfile(@PathVariable Long userId, @RequestParam String professionalType,
			@RequestParam Long cityId, @RequestBody Map<String, Object> details) {
		Profile savedProfile = profileService.createOrUpdateProfile(userId, professionalType, cityId, details);
		return ResponseEntity.ok(savedProfile);
	}

	// 2. Get Profile By User ID API (GET)
	@GetMapping("/{userId}")
	public ResponseEntity<Profile> getProfile(@PathVariable Long userId) {
		Profile profile = profileService.getProfileById(userId);
		return ResponseEntity.ok(profile); // profile null bhi ho sakta hai, status 200 rahega, body null
	}

	// 3. Basic Search (no pagination) ->
	// /api/profiles/search?city=Mumbai&professionalType=MUSICIAN
	// NOTE: `city` here stays a NAME string (e.g. "Mumbai") for a friendlier
	// search API — the service layer now joins through the City relation.
	@GetMapping("/search")
	public ResponseEntity<List<Profile>> searchProfiles(@RequestParam(required = false) String city,
			@RequestParam(required = false) String professionalType) {
		List<Profile> profiles = profileService.searchProfiles(city, professionalType);
		return ResponseEntity.ok(profiles);
	}

	// 4. Top-Rated Search with pagination ->
	// /api/profiles/top-rated?professionalType=MUSICIAN&city=Indore&state=MP&country=India&instrument=guitar&page=0&size=10
	@GetMapping("/top-rated")
	public ResponseEntity<Page<Profile>> searchTopRated(@RequestParam(required = false) String professionalType,
			@RequestParam(required = false) String city, @RequestParam(required = false) String state,
			@RequestParam(required = false) String country, @RequestParam(required = false) String instrument,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Profile> result = profileService.searchTopRatedProfiles(professionalType, city, state, country, instrument,
				pageable);
		return ResponseEntity.ok(result);
	}

	// 5. Add a Rating for a Profile
	@PostMapping("/{profileId}/ratings")
	public ResponseEntity<Profile> addRating(@PathVariable Long profileId, @RequestParam Long ratedByUserId,
			@RequestParam Integer stars, @RequestParam(required = false) String comment) {
		Profile updated = profileService.addRating(profileId, ratedByUserId, stars, comment);
		return ResponseEntity.ok(updated);
	}

	// 6. Delete Profile API (DELETE)
	@DeleteMapping("/{userId}")
	public ResponseEntity<String> deleteProfile(@PathVariable Long userId) {
		profileService.deleteProfile(userId);
		return ResponseEntity.ok("Profile deleted successfully!");
	}
}