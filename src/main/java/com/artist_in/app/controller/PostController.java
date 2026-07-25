package com.artist_in.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.post.CreatePostRequest;
import com.artist_in.app.dto.post.PostResponse;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.service.PostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping
	public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("Creating post for userId={}, mediaType={}", userId, request.getMediaType());
		PostResponse response = postService.createPost(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{postId}")
	public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
		Long viewerId = currentUserIdOrNull();
		log.debug("Fetching postId={} for viewerId={}", postId, viewerId);
		return ResponseEntity.ok(postService.getPost(postId, viewerId));
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<MessageResponse> deletePost(@PathVariable Long postId) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("Deleting postId={} requested by userId={}", postId, userId);
		postService.deletePost(postId, userId);
		return ResponseEntity.ok(MessageResponse.of("Post deleted successfully."));
	}

	@PostMapping("/{postId}/like")
	public ResponseEntity<MessageResponse> toggleLike(@PathVariable Long postId) {
		Long userId = SecurityUtils.getCurrentUserId();
		boolean liked = postService.toggleLike(postId, userId);
		log.debug("Post like toggled: postId={}, userId={}, liked={}", postId, userId, liked);
		return ResponseEntity.ok(MessageResponse.of(liked ? "LIKED" : "UNLIKED"));
	}

	@GetMapping("/feed")
	public ResponseEntity<PageResponse<PostResponse>> getFeed(Pageable pageable) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.debug("Fetching feed for userId={}, page={}", userId, pageable);
		return ResponseEntity.ok(postService.getFeed(userId, pageable));
	}

	@GetMapping("/explore")
	public ResponseEntity<PageResponse<PostResponse>> getExploreFeed(Pageable pageable) {
		Long viewerId = currentUserIdOrNull();
		log.debug("Fetching explore feed for viewerId={}, page={}", viewerId, pageable);
		return ResponseEntity.ok(postService.getExploreFeed(viewerId, pageable));
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<PageResponse<PostResponse>> getUserPosts(@PathVariable Long userId, Pageable pageable) {
		Long viewerId = currentUserIdOrNull();
		log.debug("Fetching posts for userId={}, viewerId={}, page={}", userId, viewerId, pageable);
		return ResponseEntity.ok(postService.getUserPosts(userId, viewerId, pageable));
	}

	@PostMapping("/{id}/view")
	public ResponseEntity<Void> registerView(@PathVariable Long id) {
		Long viewerId = currentUserIdOrNull();
		log.debug("Registering view for postId={}, viewerId={}", id, viewerId);
		postService.incrementViews(id, viewerId);
		return ResponseEntity.ok().build();
	}

	private Long currentUserIdOrNull() {
		try {
			return SecurityUtils.getCurrentUserId();
		} catch (Exception ex) {
			return null;
		}
	}
}