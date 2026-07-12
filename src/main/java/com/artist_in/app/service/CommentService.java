package com.artist_in.app.service;

import org.springframework.data.domain.Pageable;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.post.CommentResponse;
import com.artist_in.app.dto.post.CreateCommentRequest;

public interface CommentService {

    CommentResponse addComment(Long postId, Long authorId, CreateCommentRequest request);

    PageResponse<CommentResponse> getTopLevelComments(Long postId, Pageable pageable);

    PageResponse<CommentResponse> getReplies(Long commentId, Pageable pageable);

    void deleteComment(Long commentId, Long requesterId);

}