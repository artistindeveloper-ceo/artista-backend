package com.artist_in.app.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artist_in.app.entity.LoginDevice;

public interface LoginDeviceRepository extends JpaRepository<LoginDevice, Long> {

    Optional<LoginDevice> findByRefreshTokenAndIsActiveTrue(String refreshToken);

    Optional<LoginDevice> findByUser_IdAndDeviceId(Long userId, String deviceId);

    List<LoginDevice> findByUser_IdAndIsActiveTrue(Long userId);

    @Modifying
    @Query("update LoginDevice d set d.isActive = false, d.refreshToken = null where d.refreshToken = :refreshToken")
    int deactivateByRefreshToken(@Param("refreshToken") String refreshToken);

    @Modifying
    @Query("update LoginDevice d set d.isActive = false, d.refreshToken = null where d.user.id = :userId")
    int deactivateAllForUser(@Param("userId") Long userId);
}