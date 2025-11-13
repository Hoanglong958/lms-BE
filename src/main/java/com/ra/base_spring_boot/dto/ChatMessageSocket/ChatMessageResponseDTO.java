package com.ra.base_spring_boot.dto.ChatMessageSocket;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponseDTO {
    private Long id;
    private String roomId;
    private String sender;
    private String content;
    private Long timestamp;
    private Boolean system;
    private String type;
}
