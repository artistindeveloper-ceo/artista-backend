package com.artist_in.app.service;

import org.springframework.data.domain.Pageable;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.post.CreatePostRequest;
import com.artist_in.app.dto.post.PostResponse;
import com.artist_in.app.enums.MediaType;

public interface PostService {

    PostResponse createPost(Long authorId, CreatePostRequest request, String mediaUrl, String thumbnailUrl,
                            MediaType mediaType);

    PostResponse getPost(Long postId, Long viewerId);

    PageResponse<PostResponse> getUserPosts(Long targetUserId, Long viewerId, Pageable pageable);

    PageResponse<PostResponse> getFeed(Long viewerId, Pageable pageable);

    PageResponse<PostResponse> getExploreFeed(Long viewerId, Pageable pageable);

    void deletePost(Long postId, Long requesterId);

    boolean toggleLike(Long postId, Long userId);


    void incrementViews(Long postId, Long viewerId);

}