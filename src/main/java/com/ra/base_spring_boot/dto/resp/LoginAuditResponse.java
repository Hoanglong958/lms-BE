package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAuditResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String gmail;
    private RoleName role;
    private String ipAddress;
    private String osName;
    private String deviceId;
    private String userAgent;
    private LocalDateTime loginAt;
}
