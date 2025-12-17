package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.Gmail.EmailDTO;
import com.ra.base_spring_boot.dto.req.*;
import com.ra.base_spring_boot.dto.resp.JwtResponse;
import com.ra.base_spring_boot.dto.resp.VerifyOtpResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.PasswordResetOtp;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.repository.IPasswordResetTokenRepository;
import com.ra.base_spring_boot.repository.IPasswordResetOtpRepository;
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
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final IPasswordResetTokenService passwordResetTokenService;
    private final IPasswordResetOtpRepository passwordResetOtpRepository;
    private final GmailService gmailService;


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

        // ===== Validate Role =====
        RoleName role = RoleName.ROLE_USER; // mặc định USERa
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
                .password(passwordEncoder.encode(formRegister.getPassword())) // encode 1 lần
                .phone(formRegister.getPhone())
                .role(role)          // set theo role đã validate ở trên
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            // Lưu user vào DB
            userRepository.save(user);

            // Gửi email thông báo khi tài khoản được tạo
            gmailService.sendEmail(new EmailDTO(
                    user.getGmail(),
                    "Chào mừng tài khoản mới",
                    "user_created",
                    Map.of("username", user.getFullName())
            ));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Gmail đã tồn tại hoặc ràng buộc DB khác
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

    // ======================= OTP cho quên mật khẩu =========================
    @Override
    @Transactional
    public void forgotPasswordOtp(ForgotPasswordRequest request) {
        User user = userRepository.findByGmail(request.getGmail())
                .orElseThrow(() -> new HttpBadRequest("Gmail không tồn tại trong hệ thống!"));

        passwordResetOtpRepository.deleteByUser(user);

        String code = String.format("%06d", new Random().nextInt(1_000_000));
        PasswordResetOtp otp = PasswordResetOtp.builder()
                .user(user)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .maxAttempts(5)
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();
        passwordResetOtpRepository.save(otp);

        try {
            gmailService.sendEmail(new EmailDTO(
                    user.getGmail(),
                    "Mã OTP đặt lại mật khẩu",
                    "forgot_password_otp",
                    Map.of(
                            "username", user.getFullName() != null ? user.getFullName() : user.getGmail(),
                            "otp", code,
                            "expiredMinutes", 5
                    )
            ));
        } catch (Exception e) {
            // Tránh 500 nếu cấu hình mail lỗi: chỉ log và tiếp tục, vì OTP đã tạo trong hệ thống
            System.err.println("[WARN] Gửi email OTP thất bại, nhưng OTP đã được tạo: " + e.getMessage());
        }
    }

    @Override
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByGmail(request.getGmail())
                .orElseThrow(() -> new HttpBadRequest("Gmail không tồn tại trong hệ thống!"));

        PasswordResetOtp otp = passwordResetOtpRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new HttpBadRequest("Chưa có OTP nào được gửi!"));

        if (Boolean.TRUE.equals(otp.getIsUsed())) {
            throw new HttpBadRequest("OTP đã được sử dụng!");
        }
        if (otp.getExpiresAt() == null || otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new HttpBadRequest("OTP đã hết hạn, vui lòng yêu cầu lại!");
        }
        if (otp.getAttempts() != null && otp.getMaxAttempts() != null && otp.getAttempts() >= otp.getMaxAttempts()) {
            throw new HttpBadRequest("Bạn đã nhập sai quá số lần cho phép. Vui lòng yêu cầu OTP mới!");
        }
        if (!otp.getCode().equals(request.getOtp())) {
            otp.setAttempts((otp.getAttempts() == null ? 0 : otp.getAttempts()) + 1);
            passwordResetOtpRepository.save(otp);
            throw new HttpBadRequest("OTP không đúng!");
        }

        otp.setIsUsed(true);
        passwordResetOtpRepository.save(otp);

        CreatePasswordResetTokenRequest createReq = new CreatePasswordResetTokenRequest();
        createReq.setGmail(user.getGmail());
        var tokenResp = passwordResetTokenService.create(createReq);

        return VerifyOtpResponse.builder()
                .token(tokenResp.getToken())
                .expiresAt(tokenResp.getExpiresAt())
                .build();
    }
}
