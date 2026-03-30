package com.ra.base_spring_boot.services.auth;

import com.ra.base_spring_boot.dto.resp.LoginAuditResponse;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface ILoginAuditService {
    void record(User user, HttpServletRequest request, String deviceId);
    List<LoginAuditResponse> listByRoles(List<RoleName> roles, int limit);
    List<LoginAuditResponse> listByUser(Long userId, int limit);
}
