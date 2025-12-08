package com.ra.base_spring_boot.repository.chatv2;

import com.ra.base_spring_boot.model.chatv2.ChatMessageRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageReadRepository extends JpaRepository<ChatMessageRead, UUID> {
    Optional<ChatMessageRead> findByMessage_IdAndUserId(UUID messageId, Long userId);
    List<ChatMessageRead> findByMessage_Id(UUID messageId);
}
