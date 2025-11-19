package com.ra.base_spring_boot.dto.req;

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
    @Email(message = "Gmail không hợp lệ")
    private String gmail;

    @NotBlank(message = "Không được để trống")
    private String password;

    private String phone;

    private String role;

    private Boolean isActive;
}
