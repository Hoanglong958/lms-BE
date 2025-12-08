package com.ra.base_spring_boot.repository.chatv2;

import com.ra.base_spring_boot.model.chatv2.ChatMessage;
import com.ra.base_spring_boot.model.chatv2.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    Page<ChatMessage> findByRoomOrderByCreatedAtDesc(ChatRoom room, Pageable pageable);
    List<ChatMessage> findByRoom_IdOrderByCreatedAtDesc(UUID roomId);
    Page<ChatMessage> findByRoom_IdOrderByCreatedAtDesc(UUID roomId, Pageable pageable);
    Page<ChatMessage> findByRoom_IdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(UUID roomId, String keyword, Pageable pageable);
}
