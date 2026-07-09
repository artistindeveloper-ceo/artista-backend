package com.artist_in.app.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.post.CreatePostRequest;
import com.artist_in.app.dto.post.PostResponse;
import com.artist_in.app.entity.Follow;
import com.artist_in.app.entity.Post;
import com.artist_in.app.entity.PostLike;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.MediaType;
import com.artist_in.app.enums.NotificationType;
import com.artist_in.app.exception.ForbiddenException;
import com.artist_in.app.exception.ResourceNotFoundException;
import com.artist_in.app.repository.FollowRepository;
import com.artist_in.app.repository.PostLikeRepository;
import com.artist_in.app.repository.PostRepository;
import com.artist_in.app.util.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final PostLikeRepository postLikeRepository;
	private final FollowRepository followRepository;
	private final UserService userService;
	private final NotificationService notificationService;

	@Transactional
	public PostResponse createPost(Long authorId, CreatePostRequest request, String mediaUrl, String thumbnailUrl,
			MediaType mediaType) {
		User author = userService.getUserOrThrow(authorId);

		Post post = Post.builder().author(author).caption(request.getCaption()).mediaUrl(mediaUrl)
				.thumbnailUrl(thumbnailUrl).mediaType(mediaType == null ? MediaType.NONE : mediaType).build();

		post = postRepository.save(post);
		return toResponse(post, authorId);
	}

	@Transactional(readOnly = true)
	public PostResponse getPost(Long postId, Long viewerId) {
		Post post = getPostOrThrow(postId);
		return toResponse(post, viewerId);
	}

	@Transactional(readOnly = true)
	public PageResponse<PostResponse> getUserPosts(Long targetUserId, Long viewerId, Pageable pageable) {
		User target = userService.getUserOrThrow(targetUserId);
		Page<Post> page = postRepository.findByAuthorAndIsArchivedFalseOrderByCreatedAtDesc(target, pageable);
		return PageResponse.from(page, post -> toResponse(post, viewerId));
	}

	/** Feed = posts from people the viewer follows, plus the viewer's own posts. */
	@Transactional(readOnly = true)
	public PageResponse<PostResponse> getFeed(Long viewerId, Pageable pageable) {
		User viewer = userService.getUserOrThrow(viewerId);
		Page<Follow> following = followRepository.findByFollower(viewer, Pageable.unpaged());

		List<Long> authorIds = Stream
				.concat(Stream.of(viewerId), following.getContent().stream().map(f -> f.getFollowing().getId()))
				.collect(Collectors.toList());

		Page<Post> page = postRepository.findFeedForAuthorIds(authorIds, pageable);
		return PageResponse.from(page, post -> toResponse(post, viewerId));
	}

	/**
	 * Explore = public posts from people the viewer does NOT already follow (and
	 * excluding the viewer's own posts). Sorted by engagement then recency, so
	 * newer accounts see popular/trending public content — mirrors Instagram's
	 * Explore tab, which is distinct from Home (following-only).
	 */
	@Transactional(readOnly = true)
	public PageResponse<PostResponse> getExploreFeed(Long viewerId, Pageable pageable) {
		User viewer = userService.getUserOrThrow(viewerId);

		Page<Follow> following = followRepository.findByFollower(viewer, Pageable.unpaged());
		List<Long> excludedAuthorIds = following.getContent().stream().map(f -> f.getFollowing().getId())
				.collect(Collectors.toList());
		excludedAuthorIds.add(viewerId); // apne posts bhi exclude karo

		Page<Post> page = postRepository.findExplorePosts(excludedAuthorIds, pageable);
		return PageResponse.from(page, post -> toResponse(post, viewerId));
	}

	@Transactional
	public void deletePost(Long postId, Long requesterId) {
		Post post = getPostOrThrow(postId);
		if (!post.getAuthor().getId().equals(requesterId)) {
			throw new ForbiddenException("You can only delete your own posts.");
		}
		postRepository.delete(post);
	}

	@Transactional
	public boolean toggleLike(Long postId, Long userId) {
		Post post = getPostOrThrow(postId);
		User user = userService.getUserOrThrow(userId);

		boolean alreadyLiked = postLikeRepository.existsByPostAndUser(post, user);
		if (alreadyLiked) {
			postLikeRepository.deleteByPostAndUser(post, user);
			post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
			postRepository.save(post);
			return false;
		} else {
			PostLike like = PostLike.builder().post(post).user(user).createdAt(Instant.now()).build();
			postLikeRepository.save(like);
			post.setLikeCount(post.getLikeCount() + 1);
			postRepository.save(post);

			notificationService.notify(post.getAuthor(), user, NotificationType.POST_LIKED, post.getId(),
					user.getDisplayName() + " liked your post.");
			return true;
		}
	}

	public Post getPostOrThrow(Long postId) {
		return postRepository.findById(postId).orElseThrow(() -> ResourceNotFoundException.of("Post", postId));
	}

	private PostResponse toResponse(Post post, Long viewerId) {
		boolean likedByViewer = viewerId != null
				&& postLikeRepository.existsByPostAndUser(post, userService.getUserOrThrow(viewerId));

		return PostResponse.builder().id(post.getId()).author(UserMapper.toSummary(post.getAuthor()))
				.caption(post.getCaption()).mediaUrl(post.getMediaUrl()).thumbnailUrl(post.getThumbnailUrl())
				.mediaType(post.getMediaType()).likeCount(post.getLikeCount()).commentCount(post.getCommentCount())
				.likedByViewer(likedByViewer).viewsCount(post.getViewsCount()) // ← NEW
				.createdAt(post.getCreatedAt()).build();
	}

	@Transactional
	public void incrementViews(Long postId, Long viewerId) {
		Post post = getPostOrThrow(postId);

		// Apni khud ki post dekhne pe view count nahi badhna chahiye
		if (viewerId != null && post.getAuthor().getId().equals(viewerId)) {
			return;
		}

		postRepository.incrementViewCount(postId);
	}

}
