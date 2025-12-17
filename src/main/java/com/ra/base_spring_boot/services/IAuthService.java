package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.*;
import com.ra.base_spring_boot.dto.resp.JwtResponse;
import com.ra.base_spring_boot.dto.resp.VerifyOtpResponse;

public interface IAuthService
{

    void register(FormRegister formRegister);

    JwtResponse login(FormLogin formLogin);

    void changePassword(String username, ChangePasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void forgotPasswordOtp(ForgotPasswordRequest request);
    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);


}
