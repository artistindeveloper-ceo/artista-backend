package com.artist_in.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artist_in.app.entity.Notification;
import com.artist_in.app.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

	long countByRecipientAndIsReadFalse(User recipient);

	@Modifying
	@Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
	int markAllReadForRecipient(@Param("recipient") User recipient);
}
