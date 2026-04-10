package com.ra.base_spring_boot.services.chat;

import com.ra.base_spring_boot.dto.chatv2.SendMessageRequest;
import com.ra.base_spring_boot.model.chatv2.ChatMessage;
import com.ra.base_spring_boot.model.chatv2.ChatMessageType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IChatMessageService {
    ChatMessage send(SendMessageRequest req);
    Page<ChatMessage> history(UUID roomId, Pageable pageable);
    Page<ChatMessage> attachments(UUID roomId, ChatMessageType type, Pageable pageable);
    void markRead(UUID messageId, Long userId);
    void markReadAll(UUID roomId, Long userId);
    void deleteForMe(UUID messageId, Long userId);
    void deleteForAll(UUID messageId, Long operatorUserId);
    Page<ChatMessage> search(UUID roomId, String keyword, Pageable pageable);
    long unreadCount(UUID roomId, Long userId);
    long totalUnreadCount(Long userId);
}
