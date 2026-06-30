package com.artist_in.app.repository;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artist_in.app.entity.Post;
import com.artist_in.app.entity.User;

public interface PostRepository extends JpaRepository<Post, Long> {

	Page<Post> findByAuthorAndIsArchivedFalseOrderByCreatedAtDesc(User author, Pageable pageable);

	@Query("SELECT p FROM Post p WHERE p.author.id IN :authorIds AND p.isArchived = false ORDER BY p.createdAt DESC")
	Page<Post> findFeedForAuthorIds(@Param("authorIds") Collection<Long> authorIds, Pageable pageable);

	Page<Post> findByIsArchivedFalseOrderByCreatedAtDesc(Pageable pageable);
}
