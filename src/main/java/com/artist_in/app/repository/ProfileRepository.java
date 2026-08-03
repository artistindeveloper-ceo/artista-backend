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

	List<Profile> findByUser_City_NameIgnoreCaseAndProfileCategory_CodeIgnoreCase(String cityName,
			String profileCategoryCode);

	List<Profile> findByProfileCategory_CodeIgnoreCase(String profileCategoryCode);

	List<Profile> findByUser_City_NameIgnoreCase(String cityName);

	@Query(value = "SELECT p.* FROM profiles p " + "JOIN users u ON p.user_id = u.id "
			+ "JOIN city c ON u.city_id = c.id " + "JOIN state s ON c.state_id = s.id "
			+ "JOIN country co ON s.country_id = co.id "
			+ "LEFT JOIN professional_categories pc ON p.profile_category_id = pc.id "
			+ "WHERE (:professionalType IS NULL OR UPPER(pc.code) = UPPER(:professionalType)) "
			+ "AND (:city IS NULL OR c.name LIKE :city) " + "AND (:state IS NULL OR s.name LIKE :state) "
			+ "AND (:country IS NULL OR co.name LIKE :country) "
			+ "AND (:instrument IS NULL OR p.details ->> 'primaryInstrument' LIKE :instrument) "
			+ "ORDER BY p.avg_rating DESC NULLS LAST", countQuery = "SELECT COUNT(*) FROM profiles p "
					+ "JOIN users u ON p.user_id = u.id " + "JOIN city c ON u.city_id = c.id "
					+ "JOIN state s ON c.state_id = s.id " + "JOIN country co ON s.country_id = co.id "
					+ "LEFT JOIN professional_categories pc ON p.profile_category_id = pc.id "
					+ "WHERE (:professionalType IS NULL OR UPPER(pc.code) = UPPER(:professionalType)) "
					+ "AND (:city IS NULL OR c.name ILIKE :city) " + "AND (:state IS NULL OR s.name ILIKE :state) "
					+ "AND (:country IS NULL OR co.name ILIKE :country) "
					+ "AND (:instrument IS NULL OR p.details ->> 'primaryInstrument' ILIKE :instrument)", nativeQuery = true)
	Page<Profile> searchTopRatedProfiles(@Param("professionalType") String professionalType, @Param("city") String city,
			@Param("state") String state, @Param("country") String country, @Param("instrument") String instrument,
			Pageable pageable);

	boolean existsByUser_Id(Long userId);
}