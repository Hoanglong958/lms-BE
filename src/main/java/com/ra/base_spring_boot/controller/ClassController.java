package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.Classroom.ClassStatsResponseDTO;
import com.ra.base_spring_boot.dto.Classroom.ClassroomRequestDTO;
import com.ra.base_spring_boot.dto.Classroom.ClassroomResponseDTO;
import com.ra.base_spring_boot.services.classroom.IClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "13 - Classes", description = "Quản lý lớp học, học viên, giảng viên và khóa học")
public class ClassController {

    private final IClassService classroomService;

    // =========== CRUD lớp học ===========
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER')")
    @Operation(summary = "Tạo lớp học mới")
    public ResponseEntity<ClassroomResponseDTO> create(@RequestBody ClassroomRequestDTO dto) {
        return ResponseEntity.ok(classroomService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER')")
    @Operation(summary = "Cập nhật lớp học")
    public ResponseEntity<ClassroomResponseDTO> update(@PathVariable Long id,
            @RequestBody ClassroomRequestDTO dto) {
        return ResponseEntity.ok(classroomService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER')")
    @Operation(summary = "Xóa lớp học")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classroomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Chi tiết lớp học")
    public ResponseEntity<ClassroomResponseDTO> findById(@RequestParam Long id) {
        return ResponseEntity.ok(classroomService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Danh sách lớp học")
    public ResponseEntity<List<ClassroomResponseDTO>> findAll() {
        return ResponseEntity.ok(classroomService.findAll());
    }

    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Danh sách lớp học (phân trang)")
    public ResponseEntity<Page<ClassroomResponseDTO>> paging(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {
        String[] sortParts = sort != null ? sort.split(",") : new String[] {};
        String property = (sortParts.length >= 1 && sortParts[0] != null && !sortParts[0].isBlank()) ? sortParts[0]
                : "createdAt";
        String directionStr = (sortParts.length >= 2 && sortParts[1] != null && !sortParts[1].isBlank()) ? sortParts[1]
                : "DESC";
        Sort.Direction direction = Sort.Direction.fromString(Objects.requireNonNull(directionStr));
        Sort sortObj = Sort.by(direction, property);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<ClassroomResponseDTO> result = (keyword != null && !keyword.isBlank())
                ? classroomService.search(keyword, pageable)
                : classroomService.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    // =========== Thống kê lớp học ===========
    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TEACHER','ROLE_USER')")
    @Operation(summary = "Thống kê các chỉ số trong lớp học", description = "Trả về sĩ số, số HV active/completed/dropped, điểm trung bình và tỉ lệ điểm danh")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thống kê lớp học", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassStatsResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy lớp học", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<ClassStatsResponseDTO> getClassStats(@RequestParam Long id) {
        return ResponseEntity.ok(classroomService.getClassStats(id));
    }

    @GetMapping("/my-classes")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    @Operation(summary = "Danh sách lớp học của giảng viên hiện tại")
    public ResponseEntity<List<ClassroomResponseDTO>> getMyClasses() {
        // Lấy user từ Security Context
        com.ra.base_spring_boot.security.principle.MyUserDetails userPrincipal = (com.ra.base_spring_boot.security.principle.MyUserDetails) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(classroomService.findClassesByTeacher(userPrincipal.getUser().getId()));
    }
}
