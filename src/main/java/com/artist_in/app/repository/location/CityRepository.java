package com.artist_in.app.repository.location;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artist_in.app.entity.location.City;

public interface CityRepository extends JpaRepository<City, Long> {

	List<City> findByStateIdOrderByNameAsc(Long stateId);

	@Query("SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY c.name ASC")
	List<City> searchByName(@Param("query") String query);
}