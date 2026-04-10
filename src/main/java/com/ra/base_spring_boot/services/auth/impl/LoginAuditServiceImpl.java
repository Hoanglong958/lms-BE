package com.ra.base_spring_boot.services.auth.impl;

import com.ra.base_spring_boot.dto.resp.LoginAuditResponse;
import com.ra.base_spring_boot.model.LoginAudit;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.auth.ILoginAuditRepository;
import com.ra.base_spring_boot.services.auth.ILoginAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginAuditServiceImpl implements ILoginAuditService {

    private final ILoginAuditRepository loginAuditRepository;

    @Override
    @Transactional
    public void record(User user, HttpServletRequest request, String deviceId) {
        if (user == null) return;

        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String ip = extractClientIp(request);
        String osName = parseOsFromUserAgent(userAgent);

        LoginAudit audit = LoginAudit.builder()
                .user(user)
                .role(user.getRole())
                .ipAddress(ip)
                .userAgent(userAgent)
                .osName(osName)
                .deviceId(deviceId)
                .loginAt(LocalDateTime.now())
                .build();

        loginAuditRepository.save(audit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginAuditResponse> listByRoles(List<RoleName> roles, int limit) {
        List<LoginAudit> audits = loginAuditRepository.findTop100ByRoleInOrderByLoginAtDesc(roles);
        return audits.stream()
                .limit(limit > 0 ? limit : 50)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginAuditResponse> listByUser(Long userId, int limit) {
        List<LoginAudit> audits = loginAuditRepository.findTop50ByUser_IdOrderByLoginAtDesc(userId);
        return audits.stream()
                .limit(limit > 0 ? limit : 20)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LoginAuditResponse mapToResponse(LoginAudit audit) {
        User u = audit.getUser();
        return LoginAuditResponse.builder()
                .id(audit.getId())
                .userId(u != null ? u.getId() : null)
                .fullName(u != null ? u.getFullName() : null)
                .gmail(u != null ? u.getGmail() : null)
                .role(audit.getRole())
                .ipAddress(audit.getIpAddress())
                .osName(audit.getOsName())
                .deviceId(audit.getDeviceId())
                .userAgent(audit.getUserAgent())
                .loginAt(audit.getLoginAt())
                .build();
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP"};
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String parseOsFromUserAgent(String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os") || ua.contains("macintosh")) return "macOS";
        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
        if (ua.contains("android")) return "Android";
        if (ua.contains("linux")) return "Linux";
        return "Unknown";
    }
}
