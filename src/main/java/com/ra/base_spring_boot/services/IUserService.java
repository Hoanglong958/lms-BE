package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.UserCreateRequest;
import com.ra.base_spring_boot.dto.req.UserUpdateRequest;
import com.ra.base_spring_boot.dto.resp.UserResponse;
import com.ra.base_spring_boot.model.constants.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    Page<UserResponse> search(String keyword, RoleName role, Boolean isActive, Pageable pageable);
    UserResponse getById(Long id);
    UserResponse create(UserCreateRequest req);
    UserResponse update(Long id, UserUpdateRequest req);
    void softDelete(Long id);
    void toggleStatus(Long id, boolean active);
    boolean gmailExists(String gmail);
}
