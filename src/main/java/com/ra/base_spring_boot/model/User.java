package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.constants.RoleName;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;


    @Enumerated(EnumType.STRING)
    private RoleName role;

    private Boolean isActive;

    private LocalDateTime createdAt;

    // ✅ Thêm các trường phục vụ reset mật khẩu
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
}
