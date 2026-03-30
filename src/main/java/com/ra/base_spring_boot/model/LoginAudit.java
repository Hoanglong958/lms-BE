package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.constants.RoleName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private RoleName role;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 1024)
    private String userAgent;

    @Column(name = "os_name")
    private String osName;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "login_at")
    private LocalDateTime loginAt;

    @PrePersist
    public void prePersist() {
        if (loginAt == null) {
            loginAt = LocalDateTime.now();
        }
    }
}
