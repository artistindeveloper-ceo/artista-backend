package com.artist_in.app.entity;

import com.artist_in.app.enums.FollowRequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "follow_requests", uniqueConstraints = @UniqueConstraint(name = "uk_follow_requests_pair", columnNames = {
		"requester_id", "target_id" }), indexes = {
				@Index(name = "idx_follow_requests_target", columnList = "target_id"),
				@Index(name = "idx_follow_requests_requester", columnList = "requester_id") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "requester_id", nullable = false)
	private User requester;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "target_id", nullable = false)
	private User target;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private FollowRequestStatus status = FollowRequestStatus.PENDING;
}
