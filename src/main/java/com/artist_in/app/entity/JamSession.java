package com.artist_in.app.entity;

import java.time.Instant;

import com.artist_in.app.enums.JamSessionStatus;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jam_sessions", indexes = { @Index(name = "idx_jam_sessions_leader", columnList = "leader_id"),
		@Index(name = "idx_jam_sessions_status", columnList = "status"),
		@Index(name = "idx_jam_sessions_invite_code", columnList = "invite_code") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JamSession extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(length = 500)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "leader_id", nullable = false)
	private User leader;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private JamSessionStatus status = JamSessionStatus.SCHEDULED;

	/** Short shareable code so other musicians can join, e.g. "FX7QPL". */
	@Column(name = "invite_code", nullable = false, length = 12, unique = true)
	private String inviteCode;

	/**
	 * Id of the JamSessionSong currently being played/displayed. Null if no song is
	 * active yet. Kept denormalized here for fast lookups when a participant joins
	 * mid-session.
	 */
	@Column(name = "current_song_id")
	private Long currentSongId;

	/**
	 * Current transposition offset in semitones applied on top of the song's
	 * original key, set by the leader.
	 */
	@Column(name = "current_transpose_offset", nullable = false)
	@Builder.Default
	private int currentTransposeOffset = 0;

	@Column(name = "scheduled_start_at")
	private Instant scheduledStartAt;

	@Column(name = "started_at")
	private Instant startedAt;

	@Column(name = "ended_at")
	private Instant endedAt;

	@Column(name = "is_private", nullable = false)
	@Builder.Default
	private boolean isPrivate = true;
}
