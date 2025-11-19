package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.repository.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.services.IPasswordResetTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password-reset-tokens")
@RequiredArgsConstructor
@Tag(name = "20 - Password Reset Tokens", description = "Quản lý token đặt lại mật khẩu (delayed OTP reveal flow)")
public class PasswordResetTokenController {

    private final IPasswordResetTokenService service;
    private final IPasswordResetTokenRepository tokenRepository;

    /**
     * Validate token khi user click vào link reset password
     * Frontend sẽ gọi API này để kiểm tra token trước khi hiển thị form reset password
     */
    @GetMapping("/validate")
    @Operation(summary = "Kiểm tra token", description = "Kiểm tra token từ link reset password còn hiệu lực hay không. Frontend gọi API này khi user click vào link.")
    @ApiResponse(responseCode = "200", description = "Kết quả hợp lệ (true) hoặc không hợp lệ (false)")
    public ResponseEntity<?> validate(@RequestParam String token) {
        boolean valid = service.validateToken(token);
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(valid)
                        .build()
        );
    }

    /**
     * 🔧 DEV ENDPOINT: Lấy token mới nhất để test (không cần frontend/email)
     * Chỉ dành cho development/testing - KHÔNG dùng trong production
     * 
     * NOTE: Endpoint này được public để dễ test, nhưng chỉ nên dùng trong môi trường dev
     */
    @GetMapping("/latest")
    @Operation(summary = "[DEV] Lấy token mới nhất", 
               description = "Endpoint dành cho dev để lấy token mới nhất sau khi gọi forgot-password. " +
                           "Public endpoint (không cần JWT) để dễ test. Chỉ dùng trong môi trường development.")
    @ApiResponse(responseCode = "200", description = "Trả về token mới nhất")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy token nào")
    public ResponseEntity<?> getLatestToken() {
        PasswordResetToken token = tokenRepository.findTopByOrderByCreatedAtDesc()
                .orElse(null);
        
        if (token == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseWrapper.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .code(404)
                            .data("Không tìm thấy token nào trong hệ thống")
                            .build()
            );
        }

        PasswordResetTokenResponse response = PasswordResetTokenResponse.builder()
                .id(token.getId())
                .userId(token.getUser() != null ? token.getUser().getId() : null)
                .token(token.getToken())
                .expiresAt(token.getExpiresAt())
                .isUsed(token.getIsUsed())
                .createdAt(token.getCreatedAt())
                .build();

        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(response)
                        .build()
        );
    }
}
