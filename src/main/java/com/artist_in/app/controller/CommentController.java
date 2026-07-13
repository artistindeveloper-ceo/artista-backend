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
import com.artist_in.app.dto.post.CommentResponse;
import com.artist_in.app.dto.post.CreateCommentRequest;
import com.artist_in.app.security.SecurityUtils;
import com.artist_in.app.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor

public class CommentController {

	private final CommentService commentService;

	@PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        CommentResponse response = commentService.addComment(postId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

	@GetMapping("/posts/{postId}/comments")
	public ResponseEntity<PageResponse<CommentResponse>> getComments(@PathVariable Long postId, Pageable pageable) {
		return ResponseEntity.ok(commentService.getTopLevelComments(postId, pageable));
	}

	@GetMapping("/comments/{commentId}/replies")
	public ResponseEntity<PageResponse<CommentResponse>> getReplies(@PathVariable Long commentId, Pageable pageable) {
		return ResponseEntity.ok(commentService.getReplies(commentId, pageable));
	}

	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<MessageResponse> deleteComment(@PathVariable Long commentId) {
		Long userId = SecurityUtils.getCurrentUserId();
		commentService.deleteComment(commentId, userId);
		return ResponseEntity.ok(MessageResponse.of("Comment deleted successfully."));
	}
}
