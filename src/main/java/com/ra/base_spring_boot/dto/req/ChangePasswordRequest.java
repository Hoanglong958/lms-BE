package com.ra.base_spring_boot.dto.req;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    private String oldPassword;
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/]).{8,}$",
            message = "Mật khẩu phải tối thiểu 8 ký tự và gồm chữ hoa, chữ thường, số, ký tự đặc biệt"
    )
    private String newPassword;
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
