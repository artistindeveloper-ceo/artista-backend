package com.artist_in.app.instument.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instruments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Instrument {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "brand_id", nullable = false)
	private Brand brand;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type_id", nullable = false)
	private InstrumentType instrumentType;

	@Column(name = "model", nullable = false, length = 100)
	private String model;

	@Column(name = "model_year")
	private Integer modelYear;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "image_url", length = 255)
	private String imageUrl;

	@Column(name = "thumbnail_url", length = 255)
	private String thumbnailUrl;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "specifications", columnDefinition = "jsonb")
	private JsonNode specifications;

	@Column(name = "price", precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "currency", length = 3)
	private String currency = "USD";

	@Column(name = "is_active", columnDefinition = "boolean default true")
	private Boolean isActive = true;

	@Column(name = "is_archived", columnDefinition = "boolean default false")
	private Boolean isArchived = false;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "instrument", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<InstrumentMedia> media = new ArrayList<>();

	@OneToMany(mappedBy = "instrument", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<InstrumentReview> reviews = new ArrayList<>();

	@OneToMany(mappedBy = "instrument", fetch = FetchType.LAZY)
	private List<UserInstrument> userInstruments = new ArrayList<>();
}
