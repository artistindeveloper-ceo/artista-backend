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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a Shop / Academy / School / Institute. 1:1 with a single User —
 * same pattern as Profile — because a business here is always run by exactly
 * one account. A different email/phone (i.e. a new User account) is used if
 * that person wants a second, separate business.
 *
 * Type (SHOP/ACADEMY/SCHOOL/INSTITUTE), verification, active-status, contact
 * email/phone, and cover photo all live on User.roleType / User fields — not
 * duplicated here.
 */
@Entity
@Table(name = "businesses", indexes = { @Index(name = "idx_business_city", columnList = "city_id"),
		@Index(name = "idx_business_rating", columnList = "avg_rating") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business extends BaseEntity {

	@Id
	private Long id;

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	private User owner;

	// Business/shop ka apna naam — displayName (owner ka naam) se alag ho sakta hai
	@Column(nullable = false, length = 150)
	private String name;

	// Business ka description — personal bio se alag concept hai
	@Column(columnDefinition = "TEXT")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "city_id")
	private City city;

	@Column(name = "avg_rating")
	@Builder.Default
	private Double avgRating = 0.0;

	@Column(name = "rating_count", nullable = false)
	@Builder.Default
	private Integer ratingCount = 0;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private boolean isActive = true;

	// Type-specific dynamic fields: ACADEMY -> {"coursesOffered": [...]},
	// SHOP -> {"categories": [...], "gstNumber": "..."}
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "details", columnDefinition = "jsonb")
	@Builder.Default
	private Map<String, Object> details = new HashMap<>();
}