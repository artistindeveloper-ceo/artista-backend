package com.artist_in.app.controller.location;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.location.CityDto;
import com.artist_in.app.dto.location.CountryDto;
import com.artist_in.app.dto.location.StateDto;
import com.artist_in.app.service.LocationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

	private final LocationService locationService;

	@GetMapping("/countries")
	public List<CountryDto> getCountries() {
		return locationService.getAllCountries();
	}

	@GetMapping("/countries/{countryId}/states")
	public List<StateDto> getStates(@PathVariable Long countryId) {
		return locationService.getStatesByCountry(countryId);
	}

	@GetMapping("/states/{stateId}/cities")
	public List<CityDto> getCities(@PathVariable Long stateId) {
		return locationService.getCitiesByState(stateId);
	}

	/**
	 * Used by the Flutter city-search field. GET
	 * /api/locations/cities/search?q=indo
	 */
	@GetMapping("/cities/search")
	public List<CityDto> searchCities(@RequestParam("q") String query) {
		return locationService.searchCities(query);
	}

	@GetMapping("/cities/{cityId}")
	public CityDto getCity(@PathVariable Long cityId) {
		return locationService.getCityById(cityId);
	}
}
