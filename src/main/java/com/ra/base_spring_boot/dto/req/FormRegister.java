package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FormRegister {
        @NotBlank(message = "Không được để trống")
        private String fullName;

        @NotBlank(message = "Không được để trống")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/]).{8,}$", message = "Mật khẩu phải tối thiểu 8 ký tự và gồm chữ hoa, chữ thường, số, ký tự đặc biệt")
        private String password;

        @NotBlank(message = "Không được để trống")
        private String gmail;

        @Pattern(regexp = "^(0\\d{9}|\\+84\\d{9})$", message = "Số điện thoại phải là 10 số bắt đầu bằng 0 hoặc dạng +84xxxxxxxxx")
        private String phone;

        private String role;

        private String imageUrl;
}
