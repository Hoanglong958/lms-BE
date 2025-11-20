package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.ChatMessageSocket.ChatMessageRequestDTO;
import com.ra.base_spring_boot.dto.ChatMessageSocket.ChatMessageResponseDTO;
import com.ra.base_spring_boot.model.ChatMessage;
import com.ra.base_spring_boot.repository.IChatMessageRepository;
import com.ra.base_spring_boot.services.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    private final IChatMessageRepository repository;

    /**
     * Save user message to chat_message
     */
    @Override
    public ChatMessageResponseDTO saveMessage(ChatMessageRequestDTO req) {
        ChatMessage entity = mapToEntity(req);
        ChatMessage saved = repository.save(entity);
        return toDTO(saved);
    }

    /**
     * System JOIN message -> DO NOT save DB
     */
    @Override
    public ChatMessageResponseDTO createSystemJoinMessage(String roomId, String user) {
        ChatMessageResponseDTO dto = new ChatMessageResponseDTO();
        dto.setRoomId(roomId);
        dto.setSender("system");
        dto.setContent(user + " joined the room");
        dto.setType("SYSTEM_JOIN");
        dto.setTimestamp(System.currentTimeMillis());
        return dto;  // ❌ Không lưu DB
    }

    /**
     * System SUBMIT message -> DO NOT save DB
     */
    @Override
    public ChatMessageResponseDTO createSystemSubmitMessage(String roomId, String user) {
        ChatMessageResponseDTO dto = new ChatMessageResponseDTO();
        dto.setRoomId(roomId);
        dto.setSender("system");
        dto.setContent(user + " submitted the exam");
        dto.setType("SYSTEM_SUBMIT");
        dto.setTimestamp(System.currentTimeMillis());
        return dto;  // ❌ Không lưu DB vì submit thuộc exam_attempt
    }

    // ============================
    // Helpers
    // ============================

    private ChatMessage mapToEntity(ChatMessageRequestDTO req) {
        ChatMessage entity = new ChatMessage();
        entity.setRoomId(req.getRoomId());
        entity.setSender(req.getSender());
        entity.setContent(req.getContent());
        entity.setType("USER");
        entity.setTimestamp(System.currentTimeMillis());
        return entity;
    }

    private ChatMessageResponseDTO toDTO(ChatMessage msg) {
        ChatMessageResponseDTO dto = new ChatMessageResponseDTO();
        dto.setId(msg.getId());
        dto.setRoomId(msg.getRoomId());
        dto.setSender(msg.getSender());
        dto.setContent(msg.getContent());
        dto.setTimestamp(msg.getTimestamp());
        dto.setType(msg.getType());
        return dto;
    }
}
