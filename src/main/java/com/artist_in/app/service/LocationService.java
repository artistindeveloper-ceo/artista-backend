package com.artist_in.app.service;

import java.util.List;

import com.artist_in.app.dto.location.CityDto;
import com.artist_in.app.dto.location.CountryDto;
import com.artist_in.app.dto.location.StateDto;

public interface LocationService {

	List<CountryDto> getAllCountries();

	List<StateDto> getStatesByCountry(Long countryId);

	List<CityDto> getCitiesByState(Long stateId);

	List<CityDto> searchCities(String query);

	CityDto getCityById(Long cityId);

}
