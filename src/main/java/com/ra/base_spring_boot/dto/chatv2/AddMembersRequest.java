package com.ra.base_spring_boot.dto.chatv2;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AddMembersRequest {
    private UUID roomId;
    private List<Long> memberIds;
}
