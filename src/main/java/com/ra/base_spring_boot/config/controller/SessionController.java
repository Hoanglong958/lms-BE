package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.config.dto.Session.SessionRequestDTO;
import com.ra.base_spring_boot.config.dto.Session.SessionResponseDTO;
import com.ra.base_spring_boot.services.ISessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Session", description = "Quản lý Chương học (session)")
public class SessionController {

    private final ISessionService sessionService;

    // ======= 1️⃣ Lấy danh sách session theo course =======
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Danh sách Chương học theo khóa học", description = "Trả về danh sách session thuộc một khóa học")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<List<SessionResponseDTO>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(sessionService.getByCourse(courseId));
    }

    // ======= 2️⃣ Lấy chi tiết 1 session =======
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Lấy chi tiết Chương học", description = "Trả về thông tin session theo ID")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<SessionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getById(id));
    }

    // ======= 3️⃣ Tạo mới session (ADMIN) =======
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo Chương học", description = "Chỉ ADMIN được phép tạo session mới")
    @ApiResponse(responseCode = "201", description = "Tạo thành công")
    public ResponseEntity<SessionResponseDTO> create(@Valid @RequestBody SessionRequestDTO dto) {
        SessionResponseDTO created = sessionService.create(dto);
        // Trả về HTTP 201 Created + URI mới tạo
        return ResponseEntity.created(URI.create("/api/v1/sessions/" + created.getId())).body(created);
    }

    // ======= 4️⃣ Cập nhật session (ADMIN) =======
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật Chương học", description = "Chỉ ADMIN được phép cập nhật session")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    public ResponseEntity<SessionResponseDTO> update(
            @Parameter(description = "Mã session") @PathVariable Long id,
            @Valid @RequestBody SessionRequestDTO dto
    ) {
        SessionResponseDTO updated = sessionService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    // ======= 5️⃣ Xoá session (ADMIN) =======
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa Chương học", description = "Chỉ ADMIN được phép xóa session")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
