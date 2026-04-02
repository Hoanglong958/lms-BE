package com.ra.base_spring_boot.dto.chatv2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatReadReceipt {
    private UUID roomId;
    private Long readerId;
    private List<UUID> messageIds;
}
