package com.ra.base_spring_boot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ra.base_spring_boot.model.base.BaseObject;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users") // 👈 ánh xạ đúng bảng Users trong DB
public class User extends BaseObject {

    @Column(name = "full_name")
    private String fullName;
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    private LocalDate dob;

    private String avatar;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private Boolean status = true;

    // ⚙️ Role trong DB bạn lưu dưới dạng string ('STUDENT', 'TEACHER', ...)
    @Enumerated(EnumType.STRING)
    private RoleType role; // RoleType là enum bạn sẽ tạo trong constants

    // ⚙️ Nếu project base vẫn cần roles cho security => giữ lại mối quan hệ này
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
