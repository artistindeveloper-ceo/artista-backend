package com.artist_in.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.artist_in.app.entity.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

	@Query("SELECT b FROM Business b LEFT JOIN FETCH b.owner o LEFT JOIN FETCH o.city WHERE b.id = :id AND b.isActive = true")
	Optional<Business> findActiveByIdWithCity(@Param("id") Long id);

	boolean existsByNameIgnoreCaseAndOwner_City_IdAndBusinessCategory_CodeIgnoreCase(String name, Long cityId,
			String businessCategoryCode);

	@Query("SELECT b FROM Business b LEFT JOIN FETCH b.businessCategory WHERE b.owner.id = :ownerId AND b.isActive = true")
	Optional<Business> findFirstActiveByOwnerId(@Param("ownerId") Long ownerId);

}