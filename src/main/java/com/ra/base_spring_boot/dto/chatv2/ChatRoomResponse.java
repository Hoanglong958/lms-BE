package com.ra.base_spring_boot.dto.chatv2;

import com.ra.base_spring_boot.model.chatv2.ChatRoomMember;
import com.ra.base_spring_boot.model.chatv2.ChatRoomType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponse {
    private UUID id;
    private String name;
    private String avatar;
    private ChatRoomType type;
    private Long createdBy;
    private LocalDateTime createdAt;
    
    private List<ChatRoomMember> members;
    
    // Additional fields for UI
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}
