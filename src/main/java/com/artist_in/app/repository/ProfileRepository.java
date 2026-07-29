package com.artist_in.app.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.artist_in.app.entity.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

	// These traverse the City relation (Profile.city.name) instead of a flat
	// string column — Spring Data resolves "CityName" as city.getName()
	// automatically since Profile no longer has a direct "cityName" property.
	// professionalType ab User pe hai (roleType), Profile pe nahi — isliye
	// User_RoleType path use ho raha hai.
	List<Profile> findByCity_NameIgnoreCaseAndUser_RoleTypeIgnoreCase(String cityName, String roleType);

	List<Profile> findByUser_RoleTypeIgnoreCase(String roleType);

	List<Profile> findByCity_NameIgnoreCase(String cityName);

	/**
	 * Paginated search across city/state/country/professionalType, plus an optional
	 * instrument/skill filter that reaches into the JSONB "details" column. Results
	 * are ordered by avg_rating descending, so page 0 always returns the top-rated
	 * matches first.
	 *
	 * All filters are optional (pass null to skip a filter). city/state/country and
	 * instrument use ILIKE with '%...%' patterns built in the service layer, so
	 * callers should pass already-wrapped patterns (e.g. "%mumbai%").
	 *
	 * profiles.city_id now points at the city table, so city/state/country are
	 * resolved via JOIN instead of being flat columns on profiles.
	 *
	 * professionalType ab profiles.professional_type nahi — users.role_type hai,
	 * isliye users table bhi join ki gayi hai (profiles.user_id -> users.id).
	 */
	@Query(value = "SELECT p.* FROM profiles p " + "JOIN users u ON p.user_id = u.id "
			+ "JOIN city c ON p.city_id = c.id " + "JOIN state s ON c.state_id = s.id "
			+ "JOIN country co ON s.country_id = co.id "
			+ "WHERE (:professionalType IS NULL OR UPPER(u.role_type) = UPPER(:professionalType)) "
			+ "AND (:city IS NULL OR c.name LIKE :city) " + "AND (:state IS NULL OR s.name LIKE :state) "
			+ "AND (:country IS NULL OR co.name LIKE :country) "
			+ "AND (:instrument IS NULL OR p.details ->> 'primaryInstrument' LIKE :instrument) "
			+ "ORDER BY p.avg_rating DESC NULLS LAST", countQuery = "SELECT COUNT(*) FROM profiles p "
					+ "JOIN users u ON p.user_id = u.id " + "JOIN city c ON p.city_id = c.id "
					+ "JOIN state s ON c.state_id = s.id " + "JOIN country co ON s.country_id = co.id "
					+ "WHERE (:professionalType IS NULL OR UPPER(u.role_type) = UPPER(:professionalType)) "
					+ "AND (:city IS NULL OR c.name ILIKE :city) " + "AND (:state IS NULL OR s.name ILIKE :state) "
					+ "AND (:country IS NULL OR co.name ILIKE :country) "
					+ "AND (:instrument IS NULL OR p.details ->> 'primaryInstrument' ILIKE :instrument)", nativeQuery = true)
	Page<Profile> searchTopRatedProfiles(@Param("professionalType") String professionalType, @Param("city") String city,
			@Param("state") String state, @Param("country") String country, @Param("instrument") String instrument,
			Pageable pageable);
}