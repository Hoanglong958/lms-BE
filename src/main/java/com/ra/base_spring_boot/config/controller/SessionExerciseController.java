package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.SessionExercise.SessionExerciseRequestDTO;
import com.ra.base_spring_boot.dto.SessionExercise.SessionExerciseResponseDTO;
import com.ra.base_spring_boot.services.ISessionExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/session-exercises")
@RequiredArgsConstructor
@Tag(name = "20 - Session Exercises", description = "Quản lý bài tập của từng session")
public class SessionExerciseController {

    private final ISessionExerciseService sessionExerciseService;

    @GetMapping(params = "sessionId")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Danh sách bài tập theo session", description = "Lấy tất cả bài tập thuộc 1 session cụ thể")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<List<SessionExerciseResponseDTO>> getBySession(@RequestParam Long sessionId) {
        return ResponseEntity.ok(sessionExerciseService.getBySessionId(sessionId));
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Chi tiết bài tập", description = "Lấy thông tin chi tiết của 1 bài tập theo ID")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<SessionExerciseResponseDTO> getById(@RequestParam Long id) {
        return ResponseEntity.ok(sessionExerciseService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo bài tập mới", description = "Chỉ ADMIN được phép thêm bài tập mới vào session")
    @ApiResponse(responseCode = "200", description = "Tạo thành công")
    public ResponseEntity<SessionExerciseResponseDTO> create(@RequestBody SessionExerciseRequestDTO dto) {
        return ResponseEntity.ok(sessionExerciseService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật bài tập", description = "Chỉ ADMIN được phép chỉnh sửa nội dung bài tập")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    public ResponseEntity<SessionExerciseResponseDTO> update(
            @PathVariable Long id,
            @RequestBody SessionExerciseRequestDTO dto) {
        return ResponseEntity.ok(sessionExerciseService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa bài tập", description = "Chỉ ADMIN được phép xóa bài tập khỏi session")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sessionExerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
