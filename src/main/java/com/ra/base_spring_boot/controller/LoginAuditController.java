package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.resp.LoginAuditResponse;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.services.auth.ILoginAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/login-audits")
@RequiredArgsConstructor
@Tag(name = "Admin - Login Audit", description = "Xem lịch sử đăng nhập của sinh viên/giảng viên")
public class LoginAuditController {

    private final ILoginAuditService loginAuditService;

    @GetMapping
    @Operation(summary = "Danh sách login", description = "Lọc theo role STUDENT/TEACHER, trả về IP, OS, thiết bị")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<ResponseWrapper<List<LoginAuditResponse>>> getLoginAudits(
            @RequestParam(name = "role", required = false) List<String> roleParams,
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(100) int limit) {

        List<RoleName> roles;
        if (roleParams == null || roleParams.isEmpty()) {
            roles = List.of(RoleName.ROLE_USER, RoleName.ROLE_TEACHER);
        } else {
            try {
                roles = roleParams.stream()
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(RoleName::valueOf)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                        ResponseWrapper.<List<LoginAuditResponse>>builder()
                                .status(HttpStatus.BAD_REQUEST)
                                .code(400)
                                .data(List.of())
                                .build());
            }
        }

        List<LoginAuditResponse> data = loginAuditService.listByRoles(roles, limit);
        return ResponseEntity.ok(
                ResponseWrapper.<List<LoginAuditResponse>>builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(data)
                        .build());
    }

    @GetMapping("/user")
    @Operation(summary = "Lịch sử đăng nhập theo user", description = "Trả 50 bản ghi gần nhất của user")
    public ResponseEntity<ResponseWrapper<List<LoginAuditResponse>>> getUserLoginAudits(
            @RequestParam("userId") Long userId,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) int limit) {
        List<LoginAuditResponse> data = loginAuditService.listByUser(userId, limit);
        return ResponseEntity.ok(
                ResponseWrapper.<List<LoginAuditResponse>>builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(data)
                        .build());
    }
}
