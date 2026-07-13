package com.artist_in.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.instument.entity.UserInstrument;

@Repository
public interface UserInstrumentRepository extends JpaRepository<UserInstrument, Integer> {

	List<UserInstrument> findByUserIdAndIsActiveTrue(Integer userId);

	Optional<UserInstrument> findByUserIdAndInstrumentId(Integer userId, Integer instrumentId);

	@Query("SELECT ui FROM UserInstrument ui " + "JOIN FETCH ui.instrument i " + "JOIN FETCH i.brand b "
			+ "JOIN FETCH i.instrumentType t " + "WHERE ui.user.id = :userId AND ui.isActive = true")
	List<UserInstrument> findUserInstrumentsWithDetails(@Param("userId") Integer userId);

	@Query("SELECT ui FROM UserInstrument ui "
			+ "WHERE ui.user.id = :userId AND ui.isPrimary = true AND ui.isActive = true")
	Optional<UserInstrument> findPrimaryInstrumentByUserId(@Param("userId") Integer userId);

	@Modifying
	@Transactional
	@Query("UPDATE UserInstrument ui " + "SET ui.isPrimary = false "
			+ "WHERE ui.user.id = :userId AND ui.isPrimary = true")
	void clearPrimaryInstrument(@Param("userId") Long userId);

	@Query("SELECT COUNT(ui) FROM UserInstrument ui " + "WHERE ui.instrument.id = :instrumentId AND ui.isActive = true")
	Long countUsersWithInstrument(@Param("instrumentId") Integer instrumentId);
}