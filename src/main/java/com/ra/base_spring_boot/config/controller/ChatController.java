package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.ChatMessageSocket.ChatMessageRequestDTO;
import com.ra.base_spring_boot.dto.ChatMessageSocket.ChatMessageResponseDTO;
import com.ra.base_spring_boot.services.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IChatService chatService;

    @MessageMapping("/chat") // FE send -> /app/chat
    public void handleMessages(ChatMessageRequestDTO request) {
        if (request == null || request.getType() == null) return;

        switch (request.getType()) {

            case "JOIN" -> {
                ChatMessageResponseDTO sysMsg =
                        chatService.createSystemJoinMessage(request.getRoomId(), request.getSender());
                messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), sysMsg);
            }

            case "SEND" -> {
                if (request.getContent() == null || request.getContent().trim().isEmpty()) return;

                ChatMessageResponseDTO saved = chatService.saveMessage(request);
                messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), saved);
            }

            case "SUBMIT" -> {
                ChatMessageResponseDTO sysMsg = new ChatMessageResponseDTO();
                sysMsg.setRoomId(request.getRoomId());
                sysMsg.setSender("system");
                sysMsg.setContent(request.getSender() + " đã nộp bài");
                sysMsg.setType("SYSTEM");
                sysMsg.setTimestamp(System.currentTimeMillis());

                messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), sysMsg);
            }


            default -> {
                // ignore or log invalid type
            }
        }
    }
}
