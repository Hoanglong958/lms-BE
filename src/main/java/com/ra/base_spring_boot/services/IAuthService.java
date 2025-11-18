package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.config.dto.req.*;
import com.ra.base_spring_boot.config.dto.resp.JwtResponse;

public interface IAuthService
{

    void register(FormRegister formRegister);

    JwtResponse login(FormLogin formLogin);

    void changePassword(String username, ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);


}
