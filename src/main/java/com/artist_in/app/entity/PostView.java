package com.artist_in.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "post_views", uniqueConstraints = {
		@UniqueConstraint(name = "uq_post_viewer", columnNames = { "post_id", "viewer_id" }) })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostView {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "viewer_id", nullable = false)
	private User viewer;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
}
