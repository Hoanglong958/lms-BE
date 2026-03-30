package com.ra.base_spring_boot.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FormLogin
{
    private String gmail;
    private String password;
    /**
     * Mã thiết bị (điện thoại / trình duyệt) gửi lên để truy vết đăng nhập.
     * Optional, nhưng nên truyền từ client.
     */
    private String deviceId;
}
