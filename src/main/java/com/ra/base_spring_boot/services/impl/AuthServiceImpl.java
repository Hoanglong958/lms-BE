package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.FormLogin;
import com.ra.base_spring_boot.dto.req.FormRegister;
import com.ra.base_spring_boot.dto.resp.JwtResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.entity.User;
import com.ra.base_spring_boot.enums.Role;
import com.ra.base_spring_boot.repository.IEntityUserRepository;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService
{
    private final IEntityUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Override
    public void register(FormRegister formRegister)
    {
        // email-based registration
        User user = new User();
        user.setName(formRegister.getFullName());
        user.setEmail(formRegister.getEmail());
        user.setPassword(passwordEncoder.encode(formRegister.getPassword()));
        user.setStatus(true);
        user.setRole(Role.student); // default business role
        userRepository.save(user);
    }

    @Override
    public JwtResponse login(FormLogin formLogin)
    {
        Authentication authentication;
        try
        {
            // email-based login
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(formLogin.getEmail(), formLogin.getPassword()));
        }
        catch (AuthenticationException e)
        {
            throw new HttpBadRequest("Username or password is incorrect");
        }

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        if (!Boolean.TRUE.equals(userDetails.getUser().getStatus()))
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

