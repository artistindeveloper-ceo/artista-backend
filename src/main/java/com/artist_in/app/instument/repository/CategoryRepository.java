package com.artist_in.app.instument.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.instument.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	List<Category> findByIsActiveTrueOrderByNameAsc();
}