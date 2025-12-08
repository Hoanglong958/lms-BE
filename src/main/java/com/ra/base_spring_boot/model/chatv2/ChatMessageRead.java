package com.ra.base_spring_boot.model.chatv2;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_message_reads", uniqueConstraints = @UniqueConstraint(columnNames = {"message_id","user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRead {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    private LocalDateTime readAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (readAt == null) readAt = LocalDateTime.now();
    }
}
