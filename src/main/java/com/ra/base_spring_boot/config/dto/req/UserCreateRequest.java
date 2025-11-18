package com.ra.base_spring_boot.config.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserCreateRequest {
    @NotBlank(message = "Không được để trống")
    private String fullName;

    @NotBlank(message = "Không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Không được để trống")
    private String password;

    private String role;

    private Boolean isActive;
}
