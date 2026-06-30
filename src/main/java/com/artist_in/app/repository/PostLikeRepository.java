package com.artist_in.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.artist_in.app.entity.Post;
import com.artist_in.app.entity.PostLike;
import com.artist_in.app.entity.User;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	Optional<PostLike> findByPostAndUser(Post post, User user);

	boolean existsByPostAndUser(Post post, User user);

	Page<PostLike> findByPost(Post post, Pageable pageable);

	void deleteByPostAndUser(Post post, User user);
}
