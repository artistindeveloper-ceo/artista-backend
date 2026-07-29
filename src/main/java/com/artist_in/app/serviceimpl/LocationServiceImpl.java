package com.artist_in.app.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import com.artist_in.app.dto.location.CountryDto;
import com.artist_in.app.dto.location.CityDto;
import com.artist_in.app.dto.location.StateDto;
import com.artist_in.app.entity.location.City;
import com.artist_in.app.entity.location.Country;
import com.artist_in.app.entity.location.State;
import com.artist_in.app.repository.location.CityRepository;
import com.artist_in.app.repository.location.CountryRepository;
import com.artist_in.app.repository.location.StateRepository;
import com.artist_in.app.service.LocationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

	private final CountryRepository countryRepository;
	private final StateRepository stateRepository;
	private final CityRepository cityRepository;

	public List<CountryDto> getAllCountries() {
		return countryRepository.findAll().stream().map(c -> new CountryDto(c.getId(), c.getName(), null, c.getIsoCode(), null, null))
				.toList();
	}

	public List<StateDto> getStatesByCountry(Long countryId) {
		return stateRepository.findByCountryIdOrderByNameAsc(countryId).stream()
				.map(s -> new StateDto(s.getId(), s.getName(), s.getCountry().getId())).toList();
	}

	public List<CityDto> getCitiesByState(Long stateId) {
		return cityRepository.findByStateIdOrderByNameAsc(stateId).stream().map(this::toCityDto).toList();
	}

	/**
	 * Autocomplete search — capped at 20 results so the dropdown stays fast even if
	 * the city table grows large.
	 */
	public List<CityDto> searchCities(String query) {
		if (query == null || query.trim().length() < 2) {
			return List.of();
		}
		return cityRepository.searchByName(query.trim()).stream().limit(20).map(this::toCityDto).toList();
	}

	public CityDto getCityById(Long cityId) {
		City city = cityRepository.findById(cityId)
				.orElseThrow(() -> new RuntimeException("City not found: " + cityId));
		return toCityDto(city);
	}

	private CityDto toCityDto(City city) {
		State state = city.getState();
		Country country = state.getCountry();
		return new CityDto(city.getId(), city.getName(), state.getId(), state.getName(), country.getId(),
				country.getName());
	}
}
