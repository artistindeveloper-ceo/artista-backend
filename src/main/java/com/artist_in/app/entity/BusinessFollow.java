package com.artist_in.app.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Exact same pattern as {@code Follow} (User→User), except the target is a
 * Business instead of a User. Kept as a separate table on purpose — the
 * existing musician-follows-musician feature (Follow.java) stays completely
 * untouched, this just adds the same capability for Business pages.
 */
@Entity
@Table(name = "business_follows", uniqueConstraints = @UniqueConstraint(name = "uk_business_follows_pair", columnNames = {
		"follower_id", "business_id" }), indexes = {
				@Index(name = "idx_business_follows_follower", columnList = "follower_id"),
				@Index(name = "idx_business_follows_business", columnList = "business_id") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessFollow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** The user who is doing the following. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "follower_id", nullable = false)
	private User follower;

	/** The business being followed. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "business_id", nullable = false)
	private Business business;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
}