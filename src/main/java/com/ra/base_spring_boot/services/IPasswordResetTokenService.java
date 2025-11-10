package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.CreatePasswordResetTokenRequest;
import com.ra.base_spring_boot.dto.resp.PasswordResetTokenResponse;

public interface IPasswordResetTokenService {
    PasswordResetTokenResponse create(CreatePasswordResetTokenRequest request);
    boolean validateToken(String token);
    void markUsed(String token);
}
