package com.artist_in.app.instument.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.artist_in.app.entity.User;
import com.fasterxml.jackson.databind.JsonNode;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_instruments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInstrument {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instrument_id", nullable = false)
	private Instrument instrument;

	@Column(name = "is_primary", columnDefinition = "boolean default false")
	private Boolean isPrimary = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "proficiency_level", columnDefinition = "varchar(20)")
	private ProficiencyLevel proficiencyLevel;

	@Column(name = "years_experience", precision = 3, scale = 1)
	private BigDecimal yearsExperience;

	@Column(name = "serial_number", length = 100)
	private String serialNumber;

	@Column(name = "purchase_date")
	private LocalDate purchaseDate;

	@Column(name = "notes", columnDefinition = "TEXT")
	private String notes;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "custom_details", columnDefinition = "jsonb")
	private JsonNode customDetails;

	@Column(name = "is_active", columnDefinition = "boolean default true")
	private Boolean isActive = true;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public enum ProficiencyLevel {
		beginner, intermediate, advanced, professional
	}
}
