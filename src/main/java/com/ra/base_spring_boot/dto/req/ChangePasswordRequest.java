package com.ra.base_spring_boot.dto.req;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;

}
