package com.ra.base_spring_boot.config.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserUpdateRequest {
    private String fullName;
    private String role;
    private Boolean isActive;
}
