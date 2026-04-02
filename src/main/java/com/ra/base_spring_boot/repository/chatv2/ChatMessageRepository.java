package com.ra.base_spring_boot.repository.chatv2;

import com.ra.base_spring_boot.model.chatv2.ChatMessage;
import com.ra.base_spring_boot.model.chatv2.ChatMessageType;
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

    Page<ChatMessage> findByRoom_IdAndTypeOrderByCreatedAtDesc(UUID roomId, ChatMessageType type, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.room.id = :roomId AND m.senderId != :userId AND m.isDeleted = false AND NOT EXISTS (SELECT 1 FROM ChatMessageRead r WHERE r.message.id = m.id AND r.userId = :userId)")
    long countUnreadByRoomAndUser(UUID roomId, Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.room.id IN (SELECT crm.room.id FROM ChatRoomMember crm WHERE crm.userId = :userId) AND m.senderId != :userId AND m.isDeleted = false AND NOT EXISTS (SELECT 1 FROM ChatMessageRead r WHERE r.message.id = m.id AND r.userId = :userId)")
    long countTotalUnreadByUser(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT m FROM ChatMessage m WHERE m.room.id = :roomId AND m.senderId != :userId AND m.isDeleted = false AND NOT EXISTS (SELECT 1 FROM ChatMessageRead r WHERE r.message.id = m.id AND r.userId = :userId)")
    List<ChatMessage> findUnreadMessagesInRoom(UUID roomId, Long userId);
}
