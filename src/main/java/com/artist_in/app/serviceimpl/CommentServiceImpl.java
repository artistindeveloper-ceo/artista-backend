package com.artist_in.app.serviceimpl;

import com.artist_in.app.service.CommentService;
import com.artist_in.app.service.NotificationService;
import com.artist_in.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.post.CommentResponse;
import com.artist_in.app.dto.post.CreateCommentRequest;
import com.artist_in.app.entity.Post;
import com.artist_in.app.entity.PostComment;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.NotificationType;
import com.artist_in.app.exception.BadRequestException;
import com.artist_in.app.exception.ForbiddenException;
import com.artist_in.app.exception.ResourceNotFoundException;
import com.artist_in.app.repository.PostCommentRepository;
import com.artist_in.app.repository.PostRepository;
import com.artist_in.app.util.UserMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

	private final PostCommentRepository postCommentRepository;
	private final PostRepository postRepository;
	private final UserService userService;
	private final NotificationService notificationService;

	@Override
	@Transactional
	public CommentResponse addComment(Long postId, Long authorId, CreateCommentRequest request) {

		log.info("Adding comment. PostId={}, AuthorId={}", postId, authorId);

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> ResourceNotFoundException.of("Post", postId));

		User author = userService.getUserOrThrow(authorId);

		PostComment parent = null;
		if (request.getParentCommentId() != null) {

			log.info("Reply comment detected. ParentCommentId={}", request.getParentCommentId());

			parent = postCommentRepository.findById(request.getParentCommentId())
					.orElseThrow(() -> ResourceNotFoundException.of("Comment", request.getParentCommentId()));

			if (!parent.getPost().getId().equals(postId)) {
				throw new BadRequestException("Parent comment does not belong to this post.");
			}
		}

		PostComment comment = PostComment.builder()
				.post(post)
				.author(author)
				.parentComment(parent)
				.content(request.getContent())
				.build();

		comment = postCommentRepository.save(comment);

		log.info("Comment created successfully. CommentId={}, PostId={}",
				comment.getId(), postId);

		post.setCommentCount(post.getCommentCount() + 1);
		postRepository.save(post);

		log.info("Comment count updated. PostId={}, Count={}",
				post.getId(), post.getCommentCount());

		if (parent != null) {

			log.info("Sending reply notification. CommentId={}", comment.getId());

			notificationService.notify(
					parent.getAuthor(),
					author,
					NotificationType.COMMENT_REPLIED,
					comment.getId(),
					author.getDisplayName() + " replied to your comment.");
		} else {

			log.info("Sending post comment notification. PostId={}", post.getId());

			notificationService.notify(
					post.getAuthor(),
					author,
					NotificationType.POST_COMMENTED,
					post.getId(),
					author.getDisplayName() + " commented on your post.");
		}

		log.info("Comment added successfully. CommentId={}", comment.getId());

		return toResponse(comment);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<CommentResponse> getTopLevelComments(Long postId, Pageable pageable) {

		log.info("Fetching top-level comments. PostId={}, Page={}, Size={}",
				postId, pageable.getPageNumber(), pageable.getPageSize());

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> ResourceNotFoundException.of("Post", postId));

		Page<PostComment> page = postCommentRepository
				.findByPostAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(post, pageable);

		log.info("Retrieved {} top-level comments for PostId={}",
				page.getNumberOfElements(), postId);

		return PageResponse.from(page, this::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<CommentResponse> getReplies(Long commentId, Pageable pageable) {

		log.info("Fetching replies. CommentId={}, Page={}, Size={}",
				commentId, pageable.getPageNumber(), pageable.getPageSize());

		PostComment parent = postCommentRepository.findById(commentId)
				.orElseThrow(() -> ResourceNotFoundException.of("Comment", commentId));

		Page<PostComment> page = postCommentRepository
				.findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(parent, pageable);

		log.info("Retrieved {} replies for CommentId={}",
				page.getNumberOfElements(), commentId);

		return PageResponse.from(page, this::toResponse);
	}

	@Override
	@Transactional
	public void deleteComment(Long commentId, Long requesterId) {

		log.info("Deleting comment. CommentId={}, RequesterId={}",
				commentId, requesterId);

		PostComment comment = postCommentRepository.findById(commentId)
				.orElseThrow(() -> ResourceNotFoundException.of("Comment", commentId));

		boolean isCommentAuthor = comment.getAuthor().getId().equals(requesterId);
		boolean isPostAuthor = comment.getPost().getAuthor().getId().equals(requesterId);

		if (!isCommentAuthor && !isPostAuthor) {

			log.warn("Unauthorized delete attempt. CommentId={}, RequesterId={}",
					commentId, requesterId);

			throw new ForbiddenException("You can only delete your own comments, or comments on your own posts.");
		}

		comment.setDeleted(true);
		postCommentRepository.save(comment);

		log.info("Comment marked as deleted. CommentId={}", commentId);

		Post post = comment.getPost();
		post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
		postRepository.save(post);

		log.info("Comment count updated. PostId={}, Count={}",
				post.getId(), post.getCommentCount());

		log.info("Comment deleted successfully. CommentId={}", commentId);
	}

	private CommentResponse toResponse(PostComment comment) {
		return CommentResponse.builder()
				.id(comment.getId())
				.author(UserMapper.toSummary(comment.getAuthor()))
				.content(comment.getContent())
				.parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
				.createdAt(comment.getCreatedAt())
				.build();
	}
}