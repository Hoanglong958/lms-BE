package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePasswordResetTokenRequest {
    @NotBlank(message = "Gmail không được để trống")
    @Email(message = "Gmail không hợp lệ")
    private String gmail;
}
