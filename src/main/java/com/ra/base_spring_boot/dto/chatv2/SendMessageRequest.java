package com.ra.base_spring_boot.dto.chatv2;

import com.ra.base_spring_boot.model.chatv2.ChatMessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {
    private UUID roomId;
    private Long senderId;
    private String content;
    private ChatMessageType type = ChatMessageType.TEXT;
    private String fileUrl;
}
