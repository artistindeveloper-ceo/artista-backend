package com.artist_in.app.controller;

import com.artist_in.app.service.FileStorageService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.post.CreatePostRequest;
import com.artist_in.app.dto.post.PostResponse;
import com.artist_in.app.enums.MediaType;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;
	private final FileStorageService fileStorageService;

	@PostMapping(consumes = "multipart/form-data")
	public ResponseEntity<PostResponse> createPost(@RequestParam(value = "caption", required = false) String caption,
			@RequestParam(value = "media", required = false) MultipartFile media) {
		Long userId = SecurityUtils.getCurrentUserId();
		CreatePostRequest request = new CreatePostRequest();
		request.setCaption(caption);

		String mediaUrl = null;
		String thumbnailUrl = null;
		MediaType mediaType = MediaType.NONE;

		if (media != null && !media.isEmpty()) {
			FileStorageService.StoredMedia stored = fileStorageService.storeMedia(media,
					FileStorageService.UploadCategory.POST_MEDIA);
			mediaUrl = stored.url();
			thumbnailUrl = stored.thumbnailUrl(); // ← NEW
			mediaType = stored.mediaType();
		}

		PostResponse response = postService.createPost(userId, request, mediaUrl, thumbnailUrl, mediaType);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{postId}")
	public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
		Long viewerId = currentUserIdOrNull();
		return ResponseEntity.ok(postService.getPost(postId, viewerId));
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<MessageResponse> deletePost(@PathVariable Long postId) {
		Long userId = SecurityUtils.getCurrentUserId();
		postService.deletePost(postId, userId);
		return ResponseEntity.ok(MessageResponse.of("Post deleted successfully."));
	}

	@PostMapping("/{postId}/like")
	public ResponseEntity<MessageResponse> toggleLike(@PathVariable Long postId) {
		Long userId = SecurityUtils.getCurrentUserId();
		boolean liked = postService.toggleLike(postId, userId);
		return ResponseEntity.ok(MessageResponse.of(liked ? "LIKED" : "UNLIKED"));
	}

	@GetMapping("/feed")
	public ResponseEntity<PageResponse<PostResponse>> getFeed(Pageable pageable) {
		Long userId = SecurityUtils.getCurrentUserId();
		return ResponseEntity.ok(postService.getFeed(userId, pageable));
	}

	@GetMapping("/explore")
	public ResponseEntity<PageResponse<PostResponse>> getExploreFeed(Pageable pageable) {
		Long viewerId = currentUserIdOrNull();
		return ResponseEntity.ok(postService.getExploreFeed(viewerId, pageable));
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<PageResponse<PostResponse>> getUserPosts(@PathVariable Long userId, Pageable pageable) {
		Long viewerId = currentUserIdOrNull();
		return ResponseEntity.ok(postService.getUserPosts(userId, viewerId, pageable));
	}

	@PostMapping("/{id}/view")
	public ResponseEntity<Void> registerView(@PathVariable Long id) {
		Long viewerId = currentUserIdOrNull();
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
