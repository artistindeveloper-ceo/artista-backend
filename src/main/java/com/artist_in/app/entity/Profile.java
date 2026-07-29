package com.artist_in.app.entity;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.artist_in.app.entity.location.City;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profiles", indexes = { @Index(name = "idx_profile_city", columnList = "city_id"),
		@Index(name = "idx_profile_rating", columnList = "avg_rating") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends BaseEntity {

	@Id
	private Long id;

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	private User user;

	// professionalType hataya — User.roleType hi single source of truth hai
	// (MUSICIAN, PHOTOGRAPHER, etc.)

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "city_id")
	private City city;

	@Column(name = "is_available", nullable = false)
	private boolean isAvailable = true;

	@Column(name = "avg_rating")
	private Double avgRating = 0.0;

	@Column(name = "rating_count", nullable = false)
	private Integer ratingCount = 0;

	// Role-specific dynamic data (instruments, cameraGear, equipment, etc.)
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "details", columnDefinition = "jsonb")
	private Map<String, Object> details = new HashMap<>();
}