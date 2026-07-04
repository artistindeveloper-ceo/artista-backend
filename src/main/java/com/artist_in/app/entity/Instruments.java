package com.artist_in.app.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class Instruments {

	@Id
	private String id;

	@Column(name = "instrument_name")
	private String instrumentName;

	@Column(name = "category")
	private String category;

	public String getId() {
		if (this.id == null) {
			this.id = UUID.randomUUID().toString();
		}
		return id;
	}
}