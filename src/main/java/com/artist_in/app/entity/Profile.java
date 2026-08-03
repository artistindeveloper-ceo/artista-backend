package com.artist_in.app.entity;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.artist_in.app.entity.Professional.ProfileCategory;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profiles", indexes = { @Index(name = "idx_profile_rating", columnList = "avg_rating"),
		@Index(name = "idx_profile_category", columnList = "profile_category_id") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends BaseEntity {

	@Id
	private Long id;

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_category_id")
	private ProfileCategory profileCategory;

	@Column(name = "is_available", nullable = false)
	@Builder.Default
	private boolean isAvailable = true;

	@Column(name = "avg_rating")
	@Builder.Default
	private Double avgRating = 0.0;

	@Column(name = "rating_count", nullable = false)
	@Builder.Default
	private Integer ratingCount = 0;

	// Role-specific dynamic data (instruments, cameraGear, equipment, etc.)
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "details", columnDefinition = "jsonb")
	@Builder.Default
	private Map<String, Object> details = new HashMap<>();
}