package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.UserCreateRequest;
import com.ra.base_spring_boot.dto.req.UserUpdateRequest;
import com.ra.base_spring_boot.dto.resp.UserResponse;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.services.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController // Đánh dấu đây là REST controller, trả về JSON
@RequestMapping("/api/v1/users") // Base path cho tất cả endpoint liên quan đến User
@RequiredArgsConstructor // Tự động sinh constructor cho các field final
@Tag(name = "02 - Users", description = "Quản lý người dùng") // Dùng cho Swagger/OpenAPI
public class UserController {

    private final IUserService userService; // Service xử lý logic liên quan đến User

    // ===================== Kiểm tra tồn tại gmail =====================
    @GetMapping("/check")
    @Operation(summary = "Kiểm tra tồn tại gmail", description = "Trả về true nếu gmail đã tồn tại")
    @ApiResponse(responseCode = "200", description = "OK") // Mô tả response cho Swagger
    public ResponseEntity<?> checkGmailExists(@RequestParam String gmail) {
        boolean exists = userService.gmailExists(gmail); // Gọi service kiểm tra gmail
        return ResponseEntity.ok(
                ResponseWrapper.builder() // Dùng ResponseWrapper để chuẩn hóa response
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(java.util.Map.of("exists", exists)) // Trả về object { exists: true/false }
                        .build()
        );
    }

    // ===================== Lấy danh sách người dùng =====================
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Chỉ ADMIN mới xem danh sách
    @Operation(summary = "Danh sách người dùng", description = "Tìm kiếm và lọc theo vai trò, trạng thái")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<?> listUsers(
            @RequestParam(required = false) String keyword, // Từ khóa tìm kiếm
            @RequestParam(required = false) String role, // Lọc theo vai trò
            @RequestParam(required = false) Boolean isActive, // Lọc theo trạng thái kích hoạt
            Pageable pageable // Hỗ trợ phân trang/sắp xếp
    ) {
        RoleName roleFilter = parseRole(role); // Chuyển role từ String sang enum
        Pageable safePageable = sanitizePageable(pageable);
        Page<UserResponse> page = userService.search(keyword, roleFilter, isActive, safePageable); // Gọi service tìm kiếm
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(page) // Trả về danh sách dạng Page
                        .build()
        );
    }

    // ===================== Lấy chi tiết người dùng theo ID =====================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Chỉ ADMIN mới xem chi tiết
    @Operation(summary = "Chi tiết người dùng", description = "Lấy thông tin theo id")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<?> getById(@Parameter(description = "ID người dùng") @PathVariable Long id) {
        UserResponse resp = userService.getById(id); // Gọi service lấy user theo ID
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(resp)
                        .build()
        );
    }

    // ===================== Tạo người dùng mới =====================
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Chỉ ADMIN mới tạo user
    @Operation(summary = "Tạo người dùng", description = "ADMIN tạo mới user")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<?> create(@Valid @RequestBody UserCreateRequest req) {
        UserResponse resp = userService.create(req); // Gọi service tạo user
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data(resp)
                        .build()
        );
    }

    // ===================== Cập nhật người dùng =====================
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Chỉ ADMIN mới cập nhật
    @Operation(summary = "Cập nhật người dùng", description = "ADMIN cập nhật user")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req) {
        UserResponse resp = userService.update(id, req); // Gọi service cập nhật
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(resp)
                        .build()
        );
    }

    // ===================== Vô hiệu hóa người dùng (Soft delete) =====================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Chỉ ADMIN mới xóa
    @Operation(summary = "Vô hiệu hóa người dùng", description = "Soft delete: chuyển isActive=false")
    @ApiResponse(responseCode = "204", description = "No Content") // Không có nội dung trả về
    public ResponseEntity<?> softDelete(@PathVariable Long id) {
        userService.softDelete(id); // Chuyển trạng thái isActive=false
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ===================== Thay đổi trạng thái người dùng =====================
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Chỉ ADMIN mới thay đổi trạng thái
    @Operation(summary = "Thay đổi trạng thái", description = "Bật/tắt isActive")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, @RequestParam boolean active) {
        userService.toggleStatus(id, active); // Gọi service bật/tắt trạng thái
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Cập nhật trạng thái thành công")
                        .build()
        );
    }

    // ===================== Chuyển role từ String sang Enum =====================
    private RoleName parseRole(String input) {
        if (input == null) return null;
        String s = input.trim().toUpperCase();
        if ("ADMIN".equals(s) || "ROLE_ADMIN".equals(s)) return RoleName.ROLE_ADMIN;
        if ("USER".equals(s) || "ROLE_USER".equals(s)) return RoleName.ROLE_USER;
        return null; // Nếu không hợp lệ trả về null
    }

    // ===================== Làm sạch tham số phân trang/sắp xếp =====================
    private Pageable sanitizePageable(Pageable pageable) {
        java.util.Set<String> allowed = java.util.Set.of(
                "id", "fullName", "gmail", "phone", "role", "isActive", "createdAt"
        );

        Sort incoming = pageable.getSort();
        java.util.List<Sort.Order> safeOrders = new java.util.ArrayList<>();
        for (Sort.Order o : incoming) {
            if (allowed.contains(o.getProperty())) {
                safeOrders.add(o);
            }
        }

        Sort sort = safeOrders.isEmpty()
                ? Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(safeOrders);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
