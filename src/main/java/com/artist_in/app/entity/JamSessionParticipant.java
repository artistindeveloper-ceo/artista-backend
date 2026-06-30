package com.artist_in.app.entity;

import java.time.Instant;

import com.artist_in.app.enums.ParticipantRole;

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
@Table(name = "jam_session_participants", uniqueConstraints = @UniqueConstraint(name = "uk_jam_participants_pair", columnNames = {
		"jam_session_id", "user_id" }), indexes = {
				@Index(name = "idx_jam_participants_session", columnList = "jam_session_id"),
				@Index(name = "idx_jam_participants_user", columnList = "user_id") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JamSessionParticipant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "jam_session_id", nullable = false)
	private JamSession jamSession;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private ParticipantRole role = ParticipantRole.MUSICIAN;

	@Column(name = "joined_at", nullable = false)
	private Instant joinedAt;

	@Column(name = "left_at")
	private Instant leftAt;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private boolean isActive = true;
}
