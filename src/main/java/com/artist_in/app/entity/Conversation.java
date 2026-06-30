package com.artist_in.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Direct (1:1) conversation between two users. userA/userB are stored in a
 * canonical order (lower id first) so a unique constraint can prevent duplicate
 * conversations between the same pair.
 */
@Entity
@Table(
        name = "conversations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_conversations_pair",
                columnNames = {"user_a_id", "user_b_id"}
        ),
        indexes = {
                @Index(name = "idx_conversations_user_a", columnList = "user_a_id"),
                @Index(name = "idx_conversations_user_b", columnList = "user_b_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Column(name = "last_message_at")
    private java.time.Instant lastMessageAt;

    @Column(name = "last_message_preview", length = 200)
    private String lastMessagePreview;
}
