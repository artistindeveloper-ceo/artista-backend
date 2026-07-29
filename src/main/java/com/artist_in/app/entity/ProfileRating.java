package com.artist_in.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile_ratings", indexes = { @Index(name = "idx_rating_profile", columnList = "profile_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRating extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_id", nullable = false)
	private Profile profile;

	@Column(name = "rated_by_user_id", nullable = false)
	private Long ratedByUserId;

	@Column(nullable = false)
	private Integer stars; // 1 to 5

	@Column(length = 500)
	private String comment;
}