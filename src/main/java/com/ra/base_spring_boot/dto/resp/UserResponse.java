package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String gmail;
    private String phone;
    private RoleName role;
    private String avatar;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
