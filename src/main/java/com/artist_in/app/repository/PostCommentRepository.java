package com.artist_in.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.entity.Post;
import com.artist_in.app.entity.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

	Page<PostComment> findByPostAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(Post post,
			Pageable pageable);

	Page<PostComment> findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(PostComment parentComment,
			Pageable pageable);

	long countByPostAndIsDeletedFalse(Post post);
}
