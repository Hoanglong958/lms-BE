package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.config.dto.ResponseWrapper;
import com.ra.base_spring_boot.config.dto.req.*;
import com.ra.base_spring_boot.services.IAuthService;
import com.ra.base_spring_boot.services.IPasswordResetTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Xác thực", description = "Đăng nhập, đăng ký và quản lý mật khẩu")
public class AuthController {

    private final IAuthService authService;
    private final IPasswordResetTokenService passwordResetTokenService;

    /**
     * @param formLogin FormLogin
     * @apiNote Handle login with { username , password }
     */
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Nhận JWT khi đăng nhập thành công")
    @ApiResponse(responseCode = "200", description = "Đăng nhập thành công")
    public ResponseEntity<?> handleLogin(@Valid @RequestBody FormLogin formLogin) {
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(authService.login(formLogin))
                        .build()
        );
    }

    /**
     * @param formRegister FormRegister
     * @apiNote Handle register with { fullName , email , password }
     */
    @PostMapping("/register")
    @Operation(summary = "Đăng ký", description = "Tạo tài khoản mới")
    @ApiResponse(responseCode = "201", description = "Đăng ký thành công")
    public ResponseEntity<?> handleRegister(@Valid @RequestBody FormRegister formRegister) {
        authService.register(formRegister);
        return ResponseEntity.created(URI.create("/api/v1/auth/register")).body(
                ResponseWrapper.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data("Đăng ký tài khoản thành công!")
                        .build()
        );
    }

    /**
     * @param request ChangePasswordRequest
     * @apiNote Handle change password with { oldPassword , newPassword , confirmPassword }
     */
    @PutMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu", description = "Yêu cầu xác thực bằng JWT")
    @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseWrapper.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .code(401)
                            .data("JWT không hợp lệ hoặc đã hết hạn!")
                            .build()
            );
        }

        String username = authentication.getName();
        authService.changePassword(username, request);

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Đổi mật khẩu thành công!")
                        .build()
        );
    }

    /**
     * @param request ForgotPasswordRequest
     * @apiNote Handle forgot password - send reset link via email
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi liên kết đặt lại mật khẩu qua email")
    @ApiResponse(responseCode = "200", description = "Gửi email thành công (demo có thể log ra console)")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        CreatePasswordResetTokenRequest createReq = new CreatePasswordResetTokenRequest();
        createReq.setEmail(request.getEmail());
        passwordResetTokenService.create(createReq);
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Đã tạo token đặt lại mật khẩu. Vui lòng kiểm tra email để tiếp tục.")
                        .build()
        );
    }

    /**
     * @param request ResetPasswordRequest
     * @apiNote Handle reset password with { token , newPassword }
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu", description = "Xác nhận token và đặt mật khẩu mới")
    @ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.")
                        .build()
        );
    }
}
