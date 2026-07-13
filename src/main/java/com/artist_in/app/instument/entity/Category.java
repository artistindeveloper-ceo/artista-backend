package com.artist_in.app.instument.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "name", nullable = false, unique = true, length = 50)
	private String name;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "icon", length = 100)
	private String icon;

	@Column(name = "is_active", columnDefinition = "boolean default true")
	private Boolean isActive = true;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
	private List<InstrumentType> instrumentTypes;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
