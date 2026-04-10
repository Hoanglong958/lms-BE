package com.ra.base_spring_boot.services.auth;

import com.ra.base_spring_boot.dto.req.CreatePasswordResetTokenRequest;
import com.ra.base_spring_boot.dto.req.ResetPasswordRequest;
import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;
import jakarta.validation.Valid;

public interface IPasswordResetTokenService {
    PasswordResetTokenResponse create(CreatePasswordResetTokenRequest request);
    boolean validateToken(String token);
    void markUsed(String token);

    void resetPassword(@Valid ResetPasswordRequest request);
}
