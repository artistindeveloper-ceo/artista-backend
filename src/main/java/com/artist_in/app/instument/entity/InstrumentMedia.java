package com.artist_in.app.instument.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instrument_media")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentMedia {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instrument_id", nullable = false)
	private Instrument instrument;

	@Enumerated(EnumType.STRING)
	@Column(name = "media_type", columnDefinition = "varchar(20)")
	private MediaType mediaType = MediaType.image;

	@Column(name = "url", nullable = false, length = 255)
	private String url;

	@Column(name = "is_primary", columnDefinition = "boolean default false")
	private Boolean isPrimary = false;

	@Column(name = "caption", length = 255)
	private String caption;

	@Column(name = "sort_order", columnDefinition = "integer default 0")
	private Integer sortOrder = 0;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	public enum MediaType {
		image, video, audio
	}
}
