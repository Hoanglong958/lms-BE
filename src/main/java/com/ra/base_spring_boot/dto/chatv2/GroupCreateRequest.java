package com.ra.base_spring_boot.dto.chatv2;

import lombok.Data;

import java.util.List;

@Data
public class GroupCreateRequest {
    private String name;
    private String avatar;
    private List<Long> memberIds;
    private Long createdBy;
}
