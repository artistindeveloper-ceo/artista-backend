package com.artist_in.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artist_in.app.entity.ChatMessage;
import com.artist_in.app.entity.Conversation;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	Page<ChatMessage> findByConversationAndIsDeletedFalseOrderByCreatedAtDesc(Conversation conversation,
			Pageable pageable);

	@Modifying
	@Query("UPDATE ChatMessage m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP "
			+ "WHERE m.conversation = :conversation AND m.sender.id <> :readerId AND m.isRead = false")
	int markConversationReadForUser(@Param("conversation") Conversation conversation, @Param("readerId") Long readerId);

	long countByConversationAndSenderIdNotAndIsReadFalse(Conversation conversation, Long senderId);
}
