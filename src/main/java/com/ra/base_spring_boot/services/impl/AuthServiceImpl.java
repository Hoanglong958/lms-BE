package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.config.dto.req.*;
import com.ra.base_spring_boot.config.dto.resp.JwtResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.repository.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;

    // ======================= Đăng ký =========================
    @Override
    public void register(FormRegister formRegister) {
        // ===== Validate Full Name =====
        if (formRegister.getFullName() == null || formRegister.getFullName().isBlank()) {
            throw new HttpBadRequest("Họ tên không được để trống!");
        }

        // ===== Validate Email =====
        if (formRegister.getEmail() == null || formRegister.getEmail().isBlank()) {
            throw new HttpBadRequest("Email không được để trống!");
        }

        // Regex: username >= 6 ký tự, kết thúc @gmail.com
        if (!formRegister.getEmail().matches("^[A-Za-z0-9._%+-]{6,}@gmail\\.com$")) {
            throw new HttpBadRequest("Email phải là gmail.com và phần username trước @ phải trên 5 ký tự!");
        }

        if (userRepository.existsByEmail(formRegister.getEmail())) {
            throw new HttpBadRequest("Email đã tồn tại!");
        }

        // ===== Validate Password =====
        if (formRegister.getPassword() == null || formRegister.getPassword().isBlank()) {
            throw new HttpBadRequest("Mật khẩu không được để trống!");
        }

        // Mật khẩu mạnh: ít nhất 8 ký tự, chữ hoa, chữ thường, số
        if (!formRegister.getPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")) {
            throw new HttpBadRequest("Mật khẩu phải ít nhất 8 ký tự, có chữ hoa, chữ thường và số!");
        }



        // ===== Validate Role =====
        RoleName role = RoleName.ROLE_USER; // mặc định USER
        if (formRegister.getRole() != null) {
            try {
                role = RoleName.valueOf("ROLE_" + formRegister.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new HttpBadRequest("Role không hợp lệ! Chỉ có USER hoặc ADMIN.");
            }
        }

        // ===== Tạo User =====
        User user = User.builder()
                .fullName(formRegister.getFullName())
                .email(formRegister.getEmail())
                .password(passwordEncoder.encode(formRegister.getPassword()))
                .role(role)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    // ======================= Đăng nhập =========================
    @Override
    public JwtResponse login(FormLogin formLogin) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            formLogin.getUsername(),
                            formLogin.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new HttpBadRequest("Tên đăng nhập hoặc mật khẩu không đúng!");
        }

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

        if (!userDetails.getUser().getIsActive()) {
            throw new HttpBadRequest("Tài khoản đã bị khóa!");
        }

        return JwtResponse.builder()
                .accessToken(jwtProvider.generateToken(userDetails))
                .user(userDetails.getUser())
                .roles(Set.of(userDetails.getUser().getRole().name()))
                .build();
    }

    // ======================= Đổi mật khẩu =========================
    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy người dùng!"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new HttpBadRequest("Mật khẩu cũ không đúng!");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new HttpBadRequest("Mật khẩu mới và xác nhận không khớp!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ======================= Quên mật khẩu =========================
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HttpBadRequest("Email không tồn tại trong hệ thống!"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        System.out.println("🔗 Link đặt lại mật khẩu:");
        System.out.println("http://localhost:8081/api/v1/auth/reset-password?token=" + token);
    }

    // ======================= Đặt lại mật khẩu =========================
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new HttpBadRequest("Token không hợp lệ hoặc đã sử dụng!"));

        if (Boolean.TRUE.equals(token.getIsUsed())) {
            throw new HttpBadRequest("Token đã được sử dụng!");
        }
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new HttpBadRequest("Token đã hết hạn, vui lòng yêu cầu lại!");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setIsUsed(true);
        passwordResetTokenRepository.save(token);
    }
}
