package com.ra.base_spring_boot.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Tài liệu API - Hệ thống LMS",
                version = "v1",
                description = "Tài liệu Swagger/OpenAPI cho hệ thống học tập trực tuyến.\n\n" +
                        "- Sử dụng nút Authorize để nhập JWT (Bearer Token) trước khi gọi các API yêu cầu xác thực.\n" +
                        "- Các mã phản hồi tiêu chuẩn: 200 (Thành công), 201 (Tạo mới), 400 (Yêu cầu không hợp lệ), 401 (Chưa xác thực), 403 (Không có quyền), 404 (Không tìm thấy), 500 (Lỗi hệ thống).",
                contact = @Contact(name = "Đội ngũ phát triển", email = "support@example.com")
        ),
        servers = {
                @Server(url = "/", description = "Theo origin hiện tại")
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Nhập JWT theo định dạng: Bearer <token>",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
