package com.artist_in.app.repository.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artist_in.app.entity.location.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

}
