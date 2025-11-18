package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.config.dto.ChatMessageSocket.ChatMessageRequestDTO;
import com.ra.base_spring_boot.config.dto.ChatMessageSocket.ChatMessageResponseDTO;

public interface IChatService {
    ChatMessageResponseDTO saveMessage(ChatMessageRequestDTO req);
    ChatMessageResponseDTO createSystemJoinMessage(String roomId, String user);
    ChatMessageResponseDTO createSystemSubmitMessage(String roomId, String user);
}

