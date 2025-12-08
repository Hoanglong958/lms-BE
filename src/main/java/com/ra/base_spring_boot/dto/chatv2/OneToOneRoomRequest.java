package com.ra.base_spring_boot.dto.chatv2;

import lombok.Data;

@Data
public class OneToOneRoomRequest {
    private Long userId1;
    private Long userId2;
}
