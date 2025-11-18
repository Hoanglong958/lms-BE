package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.config.dto.ResponseWrapper;
import com.ra.base_spring_boot.config.dto.req.CreatePasswordResetTokenRequest;
import com.ra.base_spring_boot.services.IPasswordResetTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password-reset-tokens")
@RequiredArgsConstructor
@Tag(name = "Password Reset Tokens", description = "Quản lý token đặt lại mật khẩu")
public class PasswordResetTokenController {

    private final IPasswordResetTokenService service;

    @PostMapping("/request")
    @Operation(summary = "Tạo token reset", description = "Nhập email để tạo token đặt lại mật khẩu")
    @ApiResponse(responseCode = "201", description = "Tạo token thành công")
    public ResponseEntity<?> create(@Valid @RequestBody CreatePasswordResetTokenRequest request) {
        var data = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/validate")
    @Operation(summary = "Kiểm tra token", description = "Kiểm tra token còn hiệu lực hay không")
    @ApiResponse(responseCode = "200", description = "Kết quả hợp lệ hoặc không hợp lệ")
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

    @PostMapping("/mark-used")
    @Operation(summary = "Đánh dấu đã dùng", description = "Đánh dấu token là đã sử dụng")
    @ApiResponse(responseCode = "200", description = "Đánh dấu thành công")
    public ResponseEntity<?> markUsed(@RequestParam String token) {
        service.markUsed(token);
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Đã đánh dấu token là đã dùng")
                        .build()
        );
    }
}
