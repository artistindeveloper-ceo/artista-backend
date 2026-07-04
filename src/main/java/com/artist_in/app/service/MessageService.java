package com.artist_in.app.service;

import org.springframework.data.domain.Pageable;

import com.artist_in.app.dto.common.PageResponse;
import com.artist_in.app.dto.message.ChatMessageResponse;
import com.artist_in.app.dto.message.ConversationResponse;
import com.artist_in.app.dto.message.SendMessageRequest;

import jakarta.validation.Valid;

public interface MessageService {

	PageResponse<ConversationResponse> getConversations(Long userId, Pageable pageable);

	PageResponse<ChatMessageResponse> getMessages(Long conversationId, Long userId, Pageable pageable);

	void markConversationRead(Long conversationId, Long userId);

	ChatMessageResponse sendMessage(Long senderId, Long recipientId, @Valid SendMessageRequest request);

	long getTotalUnreadCount(Long id);

	
}
