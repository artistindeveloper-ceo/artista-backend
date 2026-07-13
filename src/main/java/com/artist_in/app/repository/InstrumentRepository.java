package com.artist_in.app.repository;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artist_in.app.dto.instument.InstrumentProjection;
import com.artist_in.app.instument.entity.Instrument;

public interface InstrumentRepository extends JpaRepository<Instrument, Integer> {
	// Basic query methods
	List<Instrument> findByBrandIdAndIsActiveTrue(Integer brandId);

	List<Instrument> findByInstrumentTypeIdAndIsActiveTrue(Integer typeId);

	Optional<Instrument> findByBrandIdAndModel(Integer brandId, String model);

	// Custom queries with joins
	@Query("SELECT i FROM Instrument i " + "JOIN FETCH i.brand b " + "JOIN FETCH i.instrumentType t "
			+ "JOIN FETCH t.category c " + "WHERE i.isActive = true AND i.isArchived = false")
	List<Instrument> findAllInstrumentsWithDetails();

	@Query("SELECT i FROM Instrument i " + "JOIN FETCH i.brand b " + "JOIN FETCH i.instrumentType t "
			+ "JOIN FETCH t.category c " + "LEFT JOIN FETCH i.media m " + "WHERE i.id = :id AND i.isActive = true")
	Optional<Instrument> findInstrumentWithDetails(@Param("id") Integer id);

	// Search with filters
	@Query("SELECT i FROM Instrument i " + "JOIN i.brand b " + "JOIN i.instrumentType t " + "JOIN t.category c "
			+ "WHERE (:brandName IS NULL OR LOWER(b.name) = LOWER(:brandName)) "
			+ "AND (:categoryName IS NULL OR LOWER(c.name) = LOWER(:categoryName)) "
			+ "AND (:typeName IS NULL OR LOWER(t.name) = LOWER(:typeName)) "
			+ "AND (:minPrice IS NULL OR i.price >= :minPrice) " + "AND (:maxPrice IS NULL OR i.price <= :maxPrice) "
			+ "AND i.isActive = true AND i.isArchived = false")
	Page<Instrument> findInstrumentsWithFilters(@Param("brandName") String brandName,
			@Param("categoryName") String categoryName, @Param("typeName") String typeName,
			@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

	// Projection for performance
	@Query("SELECT i.id as id, i.model as model, b.name as brandName, "
			+ "t.name as typeName, i.price as price, i.imageUrl as imageUrl " + "FROM Instrument i " + "JOIN i.brand b "
			+ "JOIN i.instrumentType t " + "WHERE i.isActive = true")
	List<InstrumentProjection> findAllProjectedInstruments();

	// Most popular instruments
	@Query(value = "SELECT i.id AS id, i.model AS model, b.name AS brandName, "
			+ "t.name AS typeName, i.price AS price, i.image_url AS imageUrl, " + "COUNT(ui.id) AS usageCount "
			+ "FROM instruments i " + "JOIN brands b ON i.brand_id = b.id "
			+ "JOIN instrument_types t ON i.type_id = t.id "
			+ "LEFT JOIN user_instruments ui ON i.id = ui.instrument_id " + "WHERE i.is_active = true "
			+ "GROUP BY i.id, i.model, b.name, t.name, i.price, i.image_url "
			+ "ORDER BY usageCount DESC", nativeQuery = true)
	List<Instrument> findMostPopularInstruments(Pageable pageable);

	Page<Instrument> findByInstrumentTypeIdAndIsActiveTrue(Integer typeId, Pageable pageable);
}
