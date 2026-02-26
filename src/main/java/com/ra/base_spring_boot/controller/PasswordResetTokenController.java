package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.repository.auth.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.services.auth.IPasswordResetTokenService;
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
public class PasswordResetTokenController {

    private final IPasswordResetTokenService tokenService;
    private final IPasswordResetTokenRepository tokenRepository;

    @GetMapping("/validate")
    public ResponseEntity<ResponseWrapper<Boolean>> validateToken(@RequestParam String token) {
        boolean ok = tokenService.validateToken(token);
        return ResponseEntity.ok(ResponseWrapper.<Boolean>builder().code(200).data(ok).build());
    }

    @GetMapping("/latest")
    public ResponseEntity<ResponseWrapper<Object>> getLatest() {
        var opt = tokenRepository.findTopByOrderByCreatedAtDesc();
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseWrapper.builder().code(404).data("Không tìm thấy token nào trong hệ thống").build());
        }
        PasswordResetToken t = opt.get();
        PasswordResetTokenResponse resp = PasswordResetTokenResponse.builder()
                .id(t.getId())
                .userId(t.getUser().getId())
                .token(t.getToken())
                .expiresAt(t.getExpiresAt())
                .isUsed(t.getIsUsed())
                .createdAt(t.getCreatedAt())
                .build();

        return ResponseEntity.ok(ResponseWrapper.builder().code(200).data(resp).build());
    }
}
