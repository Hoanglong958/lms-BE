package com.ra.base_spring_boot.config.dto.resp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ra.base_spring_boot.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class JwtResponse
{
    private String accessToken;
    private final String type = "Bearer";
    @JsonIgnoreProperties({"roles","password"})
    private User user;
    private String role; // Mỗi user chỉ có 1 role duy nhất
}
