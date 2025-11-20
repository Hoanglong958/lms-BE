package com.ra.base_spring_boot.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtEntryPoint implements AuthenticationEntryPoint
{
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException
    {
        String path = request.getRequestURI();
        String message = authException.getMessage();
        
        // Thông báo lỗi rõ ràng hơn
        String errorMessage;
        if (message != null && message.contains("Full authentication is required")) {
            errorMessage = "Yêu cầu xác thực! Vui lòng đăng nhập và gửi JWT token trong header 'Authorization: Bearer <token>'";
        } else {
            errorMessage = message != null ? message : "Xác thực thất bại!";
        }
        
        log.error("❌ Unauthorized access to: {} - {}", path, errorMessage);
        response.setHeader("error", "UNAUTHORIZED");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(401);
        
        Map<String, Object> errors = new HashMap<>();
        errors.put("code", 401);
        errors.put("message", errorMessage);
        errors.put("path", path);
        errors.put("error", "UNAUTHORIZED");
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getOutputStream(), errors);
    }
}
