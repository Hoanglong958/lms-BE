package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.config.dto.ChatMessageSocket.ChatMessageRequestDTO;
import com.ra.base_spring_boot.config.dto.ChatMessageSocket.ChatMessageResponseDTO;
import com.ra.base_spring_boot.services.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IChatService chatService;

    // STOMP endpoint
    @MessageMapping("/chat")
    public void handleMessages(ChatMessageRequestDTO request) {
        processMessage(request);
    }

    // REST endpoint tạm để test
    @PostMapping("/send")
    public ResponseEntity<?> handleMessagesRest(@RequestBody ChatMessageRequestDTO request) {
        ChatMessageResponseDTO dto = processMessage(request);
        return ResponseEntity.ok(dto);
    }

    private ChatMessageResponseDTO processMessage(ChatMessageRequestDTO request) {
        if (request == null || request.getType() == null) return null;

        ChatMessageResponseDTO dto = null;

        switch (request.getType()) {
            case "JOIN" -> {
                dto = chatService.createSystemJoinMessage(request.getRoomId(), request.getSender());
                messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), dto);
            }
            case "SEND" -> {
                if (request.getContent() == null || request.getContent().trim().isEmpty()) return null;
                dto = chatService.saveMessage(request);
                messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), dto);
            }
            // ❌ Không xử lý SUBMIT ở đây nữa
            default -> {}
        }
        return dto;
    }
}
