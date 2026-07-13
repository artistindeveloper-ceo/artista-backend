package com.artist_in.app.instument.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.instument.entity.InstrumentType;

public interface InstrumentTypeRepository extends JpaRepository<InstrumentType, Integer> {
	List<InstrumentType> findByCategoryIdAndIsActiveTrueOrderByNameAsc(Integer categoryId);
}
