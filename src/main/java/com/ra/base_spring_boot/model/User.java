package com.ra.base_spring_boot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Column(name = "gmail", nullable = false, unique = true)
    private String gmail;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(unique = true)
    private String phone;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private RoleName role;

    private Boolean isActive;

    private LocalDateTime createdAt;

    // ✅ Thêm các trường phục vụ reset mật khẩu
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isActive == null) {
            this.isActive = Boolean.TRUE;
        }
    }
}
