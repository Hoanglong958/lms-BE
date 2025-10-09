package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.ChangePasswordRequest;
import com.ra.base_spring_boot.dto.req.FormLogin;
import com.ra.base_spring_boot.dto.req.FormRegister;
import com.ra.base_spring_boot.dto.resp.JwtResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Role;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.IAuthService;
import com.ra.base_spring_boot.services.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.event.ChangeEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService
{
    private final IRoleService roleService;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Override
    public void register(FormRegister formRegister) {
        // Kiểm tra username/email trùng
        if (userRepository.existsByUsername(formRegister.getUsername())) {
            throw new HttpBadRequest("Username đã tồn tại");
        }

        if (userRepository.existsByEmail(formRegister.getEmail())) {
            throw new HttpBadRequest("Email đã tồn tại");
        }

        // Lấy role từ formRegister
        Set<Role> roles = new HashSet<>();

        String inputRole = formRegister.getRole() != null ? formRegister.getRole().toUpperCase() : "ROLE_STUDENT";

        switch (inputRole) {
            case "ROLE_ADMIN":
            case "ADMIN":
                roles.add(roleService.findByRoleName(RoleName.ROLE_ADMIN));
                break;
            case "ROLE_COMPANY":
            case "COMPANY":
                roles.add(roleService.findByRoleName(RoleName.ROLE_COMPANY));
                break;
            case "ROLE_TEACHER":
            case "TEACHER":
                roles.add(roleService.findByRoleName(RoleName.ROLE_TEACHER));
                break;
            case "ROLE_STUDENT":
            case "STUDENT":
            default:
                roles.add(roleService.findByRoleName(RoleName.ROLE_STUDENT));
                break;
        }

        // Tạo user mới
        User user = User.builder()
                .fullName(formRegister.getFullName())
                .username(formRegister.getUsername())
                .email(formRegister.getEmail())
                .phone(formRegister.getPhone())
                .password(passwordEncoder.encode(formRegister.getPassword()))
                .status(true)
                .roles(roles)
                .build();

        userRepository.save(user);
    }
    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpBadRequest("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new HttpBadRequest("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    @Override
    public JwtResponse login(FormLogin formLogin)
    {
        Authentication authentication;
        try
        {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(formLogin.getUsername(), formLogin.getPassword()));
        }
        catch (AuthenticationException e)
        {
            throw new HttpBadRequest("Username or password is incorrect");
        }

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        if (!userDetails.getUser().getStatus())
        {
            throw new HttpBadRequest("your account is blocked");
        }





        return JwtResponse.builder()
                .accessToken(jwtProvider.generateToken(userDetails.getUsername()))
                .user(userDetails.getUser())
                .roles(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()))
                .build();
    }


}
