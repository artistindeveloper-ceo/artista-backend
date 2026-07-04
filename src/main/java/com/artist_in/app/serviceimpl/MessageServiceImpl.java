package com.artist_in.app.serviceimpl;

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
import com.artist_in.app.websocket.ChatMessageEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
	private final ConversationRepository conversationRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final UserService userService;
	private final NotificationService notificationService;
	private final ChatMessageEventPublisher chatMessageEventPublisher;

	@Transactional
	public Conversation getOrCreateConversation(Long userIdA, Long userIdB) {
		if (userIdA.equals(userIdB)) {
			throw new BadRequestException("You cannot start a conversation with yourself.");
		}
		User userA = userService.getUserOrThrow(userIdA);
		User userB = userService.getUserOrThrow(userIdB);

		// Canonical ordering by id so the unique constraint prevents duplicate threads.
		User first = userA.getId() < userB.getId() ? userA : userB;
		User second = userA.getId() < userB.getId() ? userB : userA;

		return conversationRepository.findByUserAAndUserB(first, second).orElseGet(
				() -> conversationRepository.save(Conversation.builder().userA(first).userB(second).build()));
	}

	@Transactional
	public ChatMessageResponse sendMessage(Long senderId, Long recipientId, SendMessageRequest request) {
		Conversation conversation = getOrCreateConversation(senderId, recipientId);
		User sender = userService.getUserOrThrow(senderId);
		User recipient = userService.getUserOrThrow(recipientId);

		ChatMessage message = ChatMessage.builder().conversation(conversation).sender(sender)
				.content(request.getContent()).attachmentUrl(request.getAttachmentUrl()).isRead(false).build();
		message = chatMessageRepository.save(message);

		conversation.setLastMessageAt(message.getCreatedAt());
		conversation.setLastMessagePreview(
				request.getContent().length() > 200 ? request.getContent().substring(0, 200) : request.getContent());
		conversationRepository.save(conversation);

		notificationService.notify(recipient, sender, NotificationType.NEW_MESSAGE, conversation.getId(),
				sender.getDisplayName() + " sent you a message.");

		ChatMessageResponse response = toResponse(message);
		chatMessageEventPublisher.publishToUser(recipient.getUsername(), response);
		return response;
	}

	@Transactional(readOnly = true)
	public PageResponse<ChatMessageResponse> getMessages(Long conversationId, Long requesterId, Pageable pageable) {
		Conversation conversation = getConversationOrThrow(conversationId);
		assertParticipant(conversation, requesterId);

		Page<ChatMessage> page = chatMessageRepository
				.findByConversationAndIsDeletedFalseOrderByCreatedAtDesc(conversation, pageable);
		return PageResponse.from(page, this::toResponse);
	}

	@Transactional
	public void markConversationRead(Long conversationId, Long readerId) {
		Conversation conversation = getConversationOrThrow(conversationId);
		assertParticipant(conversation, readerId);
		chatMessageRepository.markConversationReadForUser(conversation, readerId);
	}

	@Transactional(readOnly = true)
	public PageResponse<ConversationResponse> getConversations(Long userId, Pageable pageable) {
		User user = userService.getUserOrThrow(userId);
		Page<Conversation> page = conversationRepository.findAllForUser(user, pageable);
		return PageResponse.from(page, conv -> toConversationResponse(conv, userId));
	}

	private Conversation getConversationOrThrow(Long conversationId) {
		return conversationRepository.findById(conversationId)
				.orElseThrow(() -> ResourceNotFoundException.of("Conversation", conversationId));
	}

	private void assertParticipant(Conversation conversation, Long userId) {
		boolean isParticipant = conversation.getUserA().getId().equals(userId)
				|| conversation.getUserB().getId().equals(userId);
		if (!isParticipant) {
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

	@Transactional(readOnly = true)
	public long getTotalUnreadCount(Long userId) {
		return chatMessageRepository.countTotalUnreadForUser(userId);
	}
}
