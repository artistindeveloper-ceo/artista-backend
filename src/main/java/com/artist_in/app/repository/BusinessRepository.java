package com.artist_in.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.artist_in.app.entity.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

	// Single-row fetch: JOIN FETCH avoids a second query for city (no N+1)
	@Query("SELECT b FROM Business b LEFT JOIN FETCH b.city WHERE b.id = :id AND b.isActive = true")
	Optional<Business> findActiveByIdWithCid(@Param("id") Long id);

	// businessType ab Business pe nahi — User.roleType hai (owner ke through).
	// Uniqueness check ab owner ke roleType se hoti hai, city + business name ke
	// saath.
	boolean existsByNameIgnoreCaseAndCity_IdAndOwner_RoleType(String name, Long cityId, String roleType);

	// search() commented rehne diya hai jaisa tumne bheja — ye BusinessType enum
	// use karta tha, jo ab exist nahi karta. Business/city + owner.roleType filter
	// ke saath rewrite karna hoga jab search feature activate karoge.
//	@Query(value = "SELECT b FROM Business b LEFT JOIN FETCH b.city c " + "WHERE (:cityId IS NULL OR c.id = :cityId) "
//			+ "AND (:roleType IS NULL OR b.owner.roleType = :roleType) " + "AND b.isActive = true "
//			+ "ORDER BY b.avgRating DESC", countQuery = "SELECT COUNT(b) FROM Business b "
//					+ "WHERE (:cityId IS NULL OR b.city.id = :cityId) "
//					+ "AND (:roleType IS NULL OR b.owner.roleType = :roleType) " + "AND b.isActive = true")
//	Page<Business> search(@Param("cityId") Long cityId, @Param("roleType") String roleType, Pageable pageable);

	@Query("SELECT b FROM Business b LEFT JOIN FETCH b.city WHERE b.id = :id AND b.isActive = true")
	Optional<Business> findActiveByIdWithCity(@Param("id") Long id);
}