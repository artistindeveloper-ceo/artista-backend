package com.artist_in.app.repository.location;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artist_in.app.entity.location.State;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
	List<State> findByCountryIdOrderByNameAsc(Long countryId);
}
