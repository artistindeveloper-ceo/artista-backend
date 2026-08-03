package com.artist_in.app.serviceimpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.message.ChatMessageResponse;
import com.artist_in.app.dto.message.ConversationResponse;
import com.artist_in.app.dto.message.SendMessageRequest;
import com.artist_in.app.entity.ChatMessage;
import com.artist_in.app.entity.Conversation;
import com.artist_in.app.entity.User;
import com.artist_in.app.enums.NotificationType;
import com.artist_in.app.exception.BadRequestException;
import com.artist_in.app.exception.ForbiddenException;
import com.artist_in.app.exception.ResourceNotFoundException;
import com.artist_in.app.repository.ChatMessageRepository;
import com.artist_in.app.repository.ConversationRepository;
import com.artist_in.app.service.MessageService;
import com.artist_in.app.service.NotificationService;
import com.artist_in.app.service.UserService;
import com.artist_in.app.util.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
	private final ConversationRepository conversationRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final UserService userService;
	private final NotificationService notificationService;

	@Transactional
	public Conversation getOrCreateConversation(Long userIdA, Long userIdB) {
		if (userIdA.equals(userIdB)) {
			log.warn("Rejected attempt to start conversation with self, userId={}", userIdA);
			throw new BadRequestException("You cannot start a conversation with yourself.");
		}
		User userA = userService.getUserOrThrow(userIdA);
		User userB = userService.getUserOrThrow(userIdB);

		// Canonical ordering by id so the unique constraint prevents duplicate threads.
		User first = userA.getId() < userB.getId() ? userA : userB;
		User second = userA.getId() < userB.getId() ? userB : userA;

		return conversationRepository.findByUserAAndUserB(first, second).orElseGet(() -> {
			log.info("Creating new conversation between userId={} and userId={}", first.getId(), second.getId());
			return conversationRepository.save(Conversation.builder().userA(first).userB(second).build());
		});
	}

	@Override
	@Transactional
	public ChatMessageResponse sendMessage(Long senderId, Long recipientId, SendMessageRequest request) {
		log.info("Processing sendMessage from senderId={} to recipientId={}", senderId, recipientId);
		Conversation conversation = getOrCreateConversation(senderId, recipientId);
		User sender = userService.getUserOrThrow(senderId);
		User recipient = userService.getUserOrThrow(recipientId);

		ChatMessage message = ChatMessage.builder().conversation(conversation).sender(sender)
				.content(request.getContent()).attachmentUrl(request.getAttachmentUrl()).isRead(false).build();
		message = chatMessageRepository.save(message);
		log.debug("Saved chatMessage id={} in conversationId={}", message.getId(), conversation.getId());

		conversation.setLastMessageAt(message.getCreatedAt());
		conversation.setLastMessagePreview(
				request.getContent().length() > 200 ? request.getContent().substring(0, 200) : request.getContent());
		conversationRepository.save(conversation);

		notificationService.notify(recipient, sender, NotificationType.NEW_MESSAGE, conversation.getId(),
				sender.getDisplayName() + " sent you a message.");
		log.debug("Notification dispatched to recipientId={} for conversationId={}", recipientId, conversation.getId());

		ChatMessageResponse response = toResponse(message);
		log.info("Message id={} sent successfully in conversationId={}", message.getId(), conversation.getId());
		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<ChatMessageResponse> getMessages(Long conversationId, Long requesterId, Pageable pageable) {
		log.debug("Fetching messages for conversationId={}, requesterId={}, page={}", conversationId, requesterId,
				pageable);
		Conversation conversation = getConversationOrThrow(conversationId);
		assertParticipant(conversation, requesterId);

		Page<ChatMessage> page = chatMessageRepository
				.findByConversationAndIsDeletedFalseOrderByCreatedAtDesc(conversation, pageable);
		return PageResponse.from(page, this::toResponse);
	}

	@Override
	@Transactional
	public void markConversationRead(Long conversationId, Long readerId) {
		log.info("Marking conversationId={} as read for readerId={}", conversationId, readerId);
		Conversation conversation = getConversationOrThrow(conversationId);
		assertParticipant(conversation, readerId);
		chatMessageRepository.markConversationReadForUser(conversation, readerId);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<ConversationResponse> getConversations(Long userId, Pageable pageable) {
		log.debug("Fetching conversations for userId={}, page={}", userId, pageable);
		User user = userService.getUserOrThrow(userId);
		Page<Conversation> page = conversationRepository.findAllForUser(user, pageable);
		return PageResponse.from(page, conv -> toConversationResponse(conv, userId));
	}

	private Conversation getConversationOrThrow(Long conversationId) {
		return conversationRepository.findById(conversationId).orElseThrow(() -> {
			log.warn("Conversation not found, id={}", conversationId);
			return ResourceNotFoundException.of("Conversation", conversationId);
		});
	}

	private void assertParticipant(Conversation conversation, Long userId) {
		boolean isParticipant = conversation.getUserA().getId().equals(userId)
				|| conversation.getUserB().getId().equals(userId);
		if (!isParticipant) {
			log.warn("Forbidden access attempt: userId={} is not a participant in conversationId={}", userId,
					conversation.getId());
			throw new ForbiddenException("You are not a participant in this conversation.");
		}
	}

	private ConversationResponse toConversationResponse(Conversation conversation, Long viewerId) {
		User other = conversation.getUserA().getId().equals(viewerId) ? conversation.getUserB()
				: conversation.getUserA();

		long unreadCount = chatMessageRepository.countByConversationAndSenderIdNotAndIsReadFalse(conversation,
				viewerId);

		return ConversationResponse.builder().id(conversation.getId()).otherUser(UserMapper.toSummary(other))
				.lastMessagePreview(conversation.getLastMessagePreview()).lastMessageAt(conversation.getLastMessageAt())
				.unreadCount(unreadCount).build();
	}

	private ChatMessageResponse toResponse(ChatMessage message) {
		return ChatMessageResponse.builder().id(message.getId()).conversationId(message.getConversation().getId())
				.sender(UserMapper.toSummary(message.getSender())).content(message.getContent())
				.attachmentUrl(message.getAttachmentUrl()).isRead(message.isRead()).createdAt(message.getCreatedAt())
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public long getTotalUnreadCount(Long userId) {
		log.debug("Fetching total unread count for userId={}", userId);
		return chatMessageRepository.countTotalUnreadForUser(userId);
	}
}
