package com.artist_in.app.repository;

import com.artist_in.app.entity.Post;
import com.artist_in.app.entity.PostView;
import com.artist_in.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {
	boolean existsByPostAndViewer(Post post, User viewer);
}