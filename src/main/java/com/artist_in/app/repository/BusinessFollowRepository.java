package com.artist_in.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.artist_in.app.entity.BusinessFollow;

@Repository
public interface BusinessFollowRepository extends JpaRepository<BusinessFollow, Long> {

	// Unique index (follower_id, business_id) backs this — fast existence check
	boolean existsByFollower_IdAndBusiness_Id(Long followerId, Long businessId);

	long countByBusiness_Id(Long businessId);

	void deleteByFollower_IdAndBusiness_Id(Long followerId, Long businessId);

	@Query("SELECT bf FROM BusinessFollow bf JOIN FETCH bf.follower WHERE bf.business.id = :businessId")
	Page<BusinessFollow> findFollowersOfBusiness(@Param("businessId") Long businessId, Pageable pageable);
}
