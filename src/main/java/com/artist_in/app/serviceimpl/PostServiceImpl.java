package com.artist_in.app.serviceimpl;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.artist_in.app.service.NotificationService;
import com.artist_in.app.service.PostService;
import com.artist_in.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final PostLikeRepository postLikeRepository;
	private final FollowRepository followRepository;
	private final UserService userService;
	private final NotificationService notificationService;

	@Override
	@Transactional
	public PostResponse createPost(Long authorId, CreatePostRequest request, String mediaUrl, String thumbnailUrl,
								   MediaType mediaType) {
		User author = userService.getUserOrThrow(authorId);

		Post post = Post.builder().author(author).caption(request.getCaption()).mediaUrl(mediaUrl)
				.thumbnailUrl(thumbnailUrl).mediaType(mediaType == null ? MediaType.NONE : mediaType).build();

		post = postRepository.save(post);
		log.info("Post created: id={}, authorId={}, mediaType={}", post.getId(), authorId, post.getMediaType());
		return toResponse(post, authorId);
	}

	@Override
	@Transactional(readOnly = true)
	public PostResponse getPost(Long postId, Long viewerId) {
		log.debug("Fetching postId={} for viewerId={}", postId, viewerId);
		Post post = getPostOrThrow(postId);
		return toResponse(post, viewerId);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<PostResponse> getUserPosts(Long targetUserId, Long viewerId, Pageable pageable) {
		log.debug("Fetching posts for targetUserId={}, viewerId={}, page={}", targetUserId, viewerId, pageable);
		User target = userService.getUserOrThrow(targetUserId);
		Page<Post> page = postRepository.findByAuthorAndIsArchivedFalseOrderByCreatedAtDesc(target, pageable);
		return PageResponse.from(page, post -> toResponse(post, viewerId));
	}

	/** Feed = posts from people the viewer follows, plus the viewer's own posts. */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<PostResponse> getFeed(Long viewerId, Pageable pageable) {
		log.debug("Building feed for viewerId={}, page={}", viewerId, pageable);
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
	@Override
	@Transactional(readOnly = true)
	public PageResponse<PostResponse> getExploreFeed(Long viewerId, Pageable pageable) {
		log.debug("Building explore feed for viewerId={}, page={}", viewerId, pageable);
		User viewer = userService.getUserOrThrow(viewerId);

		Page<Follow> following = followRepository.findByFollower(viewer, Pageable.unpaged());
		List<Long> excludedAuthorIds = following.getContent().stream().map(f -> f.getFollowing().getId())
				.collect(Collectors.toList());
		excludedAuthorIds.add(viewerId); // apne posts bhi exclude karo

		Page<Post> page = postRepository.findExplorePosts(excludedAuthorIds, pageable);
		return PageResponse.from(page, post -> toResponse(post, viewerId));
	}

	@Override
	@Transactional
	public void deletePost(Long postId, Long requesterId) {
		Post post = getPostOrThrow(postId);
		if (!post.getAuthor().getId().equals(requesterId)) {
			log.warn("Forbidden delete attempt: postId={} by requesterId={} (author is {})", postId, requesterId,
					post.getAuthor().getId());
			throw new ForbiddenException("You can only delete your own posts.");
		}
		postRepository.delete(post);
		log.info("Post deleted: id={}, requesterId={}", postId, requesterId);
	}

	@Override
	@Transactional
	public boolean toggleLike(Long postId, Long userId) {
		Post post = getPostOrThrow(postId);
		User user = userService.getUserOrThrow(userId);

		boolean alreadyLiked = postLikeRepository.existsByPostAndUser(post, user);
		if (alreadyLiked) {
			postLikeRepository.deleteByPostAndUser(post, user);
			post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
			postRepository.save(post);
			log.debug("Post unliked: postId={}, userId={}, newLikeCount={}", postId, userId, post.getLikeCount());
			return false;
		} else {
			PostLike like = PostLike.builder().post(post).user(user).createdAt(Instant.now()).build();
			postLikeRepository.save(like);
			post.setLikeCount(post.getLikeCount() + 1);
			postRepository.save(post);
			log.debug("Post liked: postId={}, userId={}, newLikeCount={}", postId, userId, post.getLikeCount());

			notificationService.notify(post.getAuthor(), user, NotificationType.POST_LIKED, post.getId(),
					user.getDisplayName() + " liked your post.");
			return true;
		}
	}

	public Post getPostOrThrow(Long postId) {
		return postRepository.findById(postId).orElseThrow(() -> {
			log.warn("Post not found, id={}", postId);
			return ResourceNotFoundException.of("Post", postId);
		});
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

	@Override
	@Transactional
	public void incrementViews(Long postId, Long viewerId) {
		Post post = getPostOrThrow(postId);

		// Apni khud ki post dekhne pe view count nahi badhna chahiye
		if (viewerId != null && post.getAuthor().getId().equals(viewerId)) {
			log.debug("Skipping view increment: postId={} viewed by own author, viewerId={}", postId, viewerId);
			return;
		}

		postRepository.incrementViewCount(postId);
	}


}
