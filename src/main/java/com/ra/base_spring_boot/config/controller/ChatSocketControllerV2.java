package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.chatv2.SendMessageRequest;
import com.ra.base_spring_boot.model.chatv2.ChatMessage;
import com.ra.base_spring_boot.services.IChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ChatSocketControllerV2 {

    private final IChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload SendMessageRequest req) {
        ChatMessage saved = chatMessageService.send(req);
        messagingTemplate.convertAndSend("/topic/rooms/" + saved.getRoom().getId(), saved);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload SendMessageRequest req) {
        messagingTemplate.convertAndSend("/topic/rooms/" + req.getRoomId() + "/typing", Objects.requireNonNull(req.getSenderId()));
    }
}
