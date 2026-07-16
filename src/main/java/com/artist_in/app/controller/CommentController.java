package com.artist_in.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.artist_in.app.dto.common.MessageResponse;
import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.post.CommentResponse;
import com.artist_in.app.dto.post.CreateCommentRequest;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

	private final CommentService commentService;

	@PostMapping("/posts/{postId}/comments")
	public ResponseEntity<CommentResponse> addComment(
			@PathVariable Long postId,
			@Valid @RequestBody CreateCommentRequest request) {

		Long userId = SecurityUtils.getCurrentUserId();

		log.info("Received request to add comment. PostId={}, UserId={}", postId, userId);

		CommentResponse response = commentService.addComment(postId, userId, request);

		log.info("Comment created successfully. CommentId={}, PostId={}, UserId={}",
				response.getId(), postId, userId);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/posts/{postId}/comments")
	public ResponseEntity<PageResponse<CommentResponse>> getComments(
			@PathVariable Long postId,
			Pageable pageable) {

		log.info("Received request to fetch comments. PostId={}, Page={}, Size={}",
				postId, pageable.getPageNumber(), pageable.getPageSize());

		return ResponseEntity.ok(commentService.getTopLevelComments(postId, pageable));
	}

	@GetMapping("/comments/{commentId}/replies")
	public ResponseEntity<PageResponse<CommentResponse>> getReplies(
			@PathVariable Long commentId,
			Pageable pageable) {

		log.info("Received request to fetch replies. CommentId={}, Page={}, Size={}",
				commentId, pageable.getPageNumber(), pageable.getPageSize());

		return ResponseEntity.ok(commentService.getReplies(commentId, pageable));
	}

	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<MessageResponse> deleteComment(@PathVariable Long commentId) {

		Long userId = SecurityUtils.getCurrentUserId();

		log.info("Received request to delete comment. CommentId={}, UserId={}", commentId, userId);

		commentService.deleteComment(commentId, userId);

		log.info("Comment deleted successfully. CommentId={}, UserId={}", commentId, userId);

		return ResponseEntity.ok(MessageResponse.of("Comment deleted successfully."));
	}
}