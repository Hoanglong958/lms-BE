package com.ra.base_spring_boot.dto.ChatMessageSocket;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDTO {
    private String roomId;
    private String sender;
    private String content;
    private String type;
}