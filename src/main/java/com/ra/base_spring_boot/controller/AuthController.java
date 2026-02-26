package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.ChangePasswordRequest;
import com.ra.base_spring_boot.dto.req.ForgotPasswordRequest;
import com.ra.base_spring_boot.dto.req.FormLogin;
import com.ra.base_spring_boot.dto.req.FormRegister;
import com.ra.base_spring_boot.dto.req.ResetPasswordRequest;
import com.ra.base_spring_boot.dto.req.UpdateProfileRequest;
import com.ra.base_spring_boot.dto.req.VerifyOtpRequest;
import com.ra.base_spring_boot.services.auth.IAuthService;
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
@Tag(name = "01 - Auth", description = "Đăng nhập, đăng ký và quản lý mật khẩu")
public class AuthController {

    private final IAuthService authService;

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
                        .build());
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
                        .build());
    }

    /**
     * @param request ChangePasswordRequest
     * @apiNote Handle change password with { oldPassword , newPassword ,
     *          confirmPassword }
     */
    @PutMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu", description = "Yêu cầu xác thực bằng JWT")
    @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseWrapper.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .code(401)
                            .data("JWT không hợp lệ hoặc đã hết hạn!")
                            .build());
        }

        String username = authentication.getName();
        authService.changePassword(username, request);

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Đổi mật khẩu thành công!")
                        .build());
    }

    /**
     * Lấy thông tin cá nhân của người dùng hiện tại
     */
    @GetMapping("/profile")
    @Operation(summary = "Lấy hồ sơ cá nhân", description = "Lấy thông tin của người dùng đang đăng nhập")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseWrapper.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .code(401)
                            .data("Chưa đăng nhập!")
                            .build());
        }
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(authService.getProfile(authentication.getName()))
                        .build());
    }

    /**
     * Cập nhật thông tin cá nhân
     */
    @PutMapping("/profile")
    @Operation(summary = "Cập nhật hồ sơ cá nhân", description = "Chỉnh sửa thông tin Họ tên, SĐT, Ảnh đại diện")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseWrapper.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .code(401)
                            .data("Chưa đăng nhập!")
                            .build());
        }
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(authService.updateProfile(authentication.getName(), request))
                        .build());
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
                        .build());
    }

    /**
     * Gửi OTP về email để xác minh quên mật khẩu
     */
    @PostMapping("/forgot-password-otp")
    @Operation(summary = "Quên mật khẩu (OTP)", description = "Gửi OTP về email để xác minh")
    @ApiResponse(responseCode = "200", description = "Gửi OTP thành công")
    public ResponseEntity<?> forgotPasswordOtp(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPasswordOtp(request);
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Đã gửi OTP về email. Vui lòng kiểm tra email và nhập OTP để tiếp tục.")
                        .build());
    }

    /**
     * Xác minh OTP và cấp reset-token để đặt lại mật khẩu
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Xác minh OTP", description = "Khi đúng OTP sẽ trả về reset-token để đặt lại mật khẩu")
    @ApiResponse(responseCode = "200", description = "Xác minh thành công, trả reset-token")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(authService.verifyOtp(request))
                        .build());
    }
}
