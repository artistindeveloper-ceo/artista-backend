package com.artist_in.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.artist_in.app.entity.ProfileRating;

@Repository
public interface ProfileRatingRepository extends JpaRepository<ProfileRating, Long> {

	List<ProfileRating> findByProfileId(Long profileId);

	@Query("SELECT AVG(r.stars) FROM ProfileRating r WHERE r.profile.id = :profileId")
	Double calculateAverageRating(@Param("profileId") Long profileId);

	@Query("SELECT COUNT(r) FROM ProfileRating r WHERE r.profile.id = :profileId")
	Long countByProfileId(@Param("profileId") Long profileId);
}