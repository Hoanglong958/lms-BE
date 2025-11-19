package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.*;
import com.ra.base_spring_boot.dto.resp.JwtResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.repository.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.model.PasswordResetToken;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.IAuthService;
import com.ra.base_spring_boot.services.IPasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final IPasswordResetTokenService passwordResetTokenService;

    // ======================= Đăng ký =========================
    @Override
    public void register(FormRegister formRegister) {
        // ===== Validate Full Name =====
        if (formRegister.getFullName() == null || formRegister.getFullName().isBlank()) {
            throw new HttpBadRequest("Họ tên không được để trống!");
        }

        // ===== Validate Gmail =====
        if (formRegister.getGmail() == null || formRegister.getGmail().isBlank()) {
            throw new HttpBadRequest("Gmail không được để trống!");
        }

        // Regex: username >= 6 ký tự, kết thúc @gmail.com
        if (!formRegister.getGmail().matches("^[A-Za-z0-9._%+-]{6,}@gmail\\.com$")) {
            throw new HttpBadRequest("Gmail phải là gmail.com và phần username trước @ phải trên 5 ký tự!");
        }

        if (userRepository.existsByGmail(formRegister.getGmail())) {
            throw new HttpBadRequest("Gmail đã tồn tại!");
        }

      // ===== Validate Password =====
      if (formRegister.getPassword() == null || formRegister.getPassword().isBlank()) {
        throw new HttpBadRequest("Mật khẩu không được để trống!");
    }

    // Mật khẩu mạnh: ít nhất 8 ký tự, chữ hoa, chữ thường, số, ký tự đặc biệt
    String passwordRegex = "^(?=.*[0-9])" +                            // có số
                           "(?=.*[a-z])" +                            // có chữ thường
                           "(?=.*[A-Z])" +                            // có chữ hoa
                           "(?=.*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/])" +  // có ký tự đặc biệt
                           ".{8,}$";                                 // độ dài tối thiểu 8 ký tự

    if (!formRegister.getPassword().matches(passwordRegex)) {
        throw new HttpBadRequest("Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
    }
        // Phone is optional. If provided, you may validate format here (currently skipped).

        // ===== Validate Role =====
        RoleName role = RoleName.ROLE_USER; // mặc định USER
        if (formRegister.getRole() != null) {
            try {
                String normalizedRole = formRegister.getRole().trim().toUpperCase();
                if (!normalizedRole.startsWith("ROLE_")) {
                    normalizedRole = "ROLE_" + normalizedRole;
                }
                role = RoleName.valueOf(normalizedRole);
            } catch (IllegalArgumentException e) {
                throw new HttpBadRequest("Role không hợp lệ! Chỉ chấp nhận USER, TEACHER hoặc ADMIN.");
            }
        }

        // ===== Tạo User =====
        User user = User.builder()
                .fullName(formRegister.getFullName())
                .gmail(formRegister.getGmail())
                .password(passwordEncoder.encode(formRegister.getPassword()))
                .phone(formRegister.getPhone())      // <<< ĐÃ THÊM
                .role(role)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Trường hợp hiếm khi race condition hoặc ràng buộc DB khác gây lỗi 500
            throw new HttpBadRequest("Dữ liệu không hợp lệ hoặc gmail đã tồn tại!");
        }
    }

    // ======================= Đăng nhập =========================
    @Override
    public JwtResponse login(FormLogin formLogin) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            formLogin.getGmail(),
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
                .role(userDetails.getUser().getRole().name())
                .build();
    }

    // ======================= Đổi mật khẩu =========================
    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByGmail(username)
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
        // Chuyển sang luồng mới: tạo token qua PasswordResetTokenService
        CreatePasswordResetTokenRequest createReq = new CreatePasswordResetTokenRequest();
        createReq.setGmail(request.getGmail());
        var resp = passwordResetTokenService.create(createReq);
        System.out.println("🔗 Link đặt lại mật khẩu:");
        System.out.println("http://localhost:8081/api/v1/auth/reset-password?token=" + resp.getToken());
    }

    // ======================= Đặt lại mật khẩu =========================
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // ===== Validate Token =====
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new HttpBadRequest("Token không hợp lệ hoặc đã sử dụng!"));

        if (Boolean.TRUE.equals(token.getIsUsed())) {
            throw new HttpBadRequest("Token đã được sử dụng!");
        }
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new HttpBadRequest("Token đã hết hạn, vui lòng yêu cầu lại!");
        }

        // ===== Validate Password (giống như đăng ký) =====
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new HttpBadRequest("Mật khẩu mới không được để trống!");
        }

        // Mật khẩu mạnh: ít nhất 8 ký tự, chữ hoa, chữ thường, số, ký tự đặc biệt
        String passwordRegex = "^(?=.*[0-9])" +                            // có số
                               "(?=.*[a-z])" +                            // có chữ thường
                               "(?=.*[A-Z])" +                            // có chữ hoa
                               "(?=.*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/])" +  // có ký tự đặc biệt
                               ".{8,}$";                                 // độ dài tối thiểu 8 ký tự

        if (!request.getNewPassword().matches(passwordRegex)) {
            throw new HttpBadRequest("Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
        }

        // Reload User từ repository để tránh LazyInitializationException
        User user = token.getUser();
        if (user == null || user.getId() == null) {
            throw new HttpBadRequest("Token không hợp lệ!");
        }
        User userToUpdate = userRepository.findById(user.getId())
                .orElseThrow(() -> new HttpBadRequest("Người dùng không tồn tại!"));
        
        userToUpdate.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(userToUpdate);

        token.setIsUsed(true);
        passwordResetTokenRepository.save(token);
    }
}
