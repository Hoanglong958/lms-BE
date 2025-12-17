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

@RequiredArgsConstructor
public class PasswordResetTokenController {

    // Old forgot-password link-based validation endpoints have been removed per new OTP flow

}
