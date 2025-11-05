package com.ra.base_spring_boot.security.principle;

import com.ra.base_spring_boot.model.User;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MyUserDetails implements UserDetails {

    private User user;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getEmail(); // Dùng email làm username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.user.getIsActive() != null ? this.user.getIsActive() : true;
    }

    /**
     * ✅ Build MyUserDetails từ User entity — tự động thêm ROLE_ nếu thiếu
     */
    public static MyUserDetails build(User user) {
        String roleName = user.getRole().name();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));

        return MyUserDetails.builder()
                .user(user)
                .authorities(authorities)
                .build();
    }
}
