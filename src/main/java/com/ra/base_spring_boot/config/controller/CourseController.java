package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.config.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.config.dto.Course.CourseRequestDTO;
import com.ra.base_spring_boot.services.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course", description = "Quản lý khóa học")
public class CourseController {

    private final ICourseService courseService;

    // ======= Tạo khóa học (ADMIN) =======
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo khóa học", description = "Chỉ ADMIN được phép tạo mới khóa học")
    @ApiResponse(responseCode = "200", description = "Tạo thành công")
    public ResponseEntity<CourseResponseDTO> createCourse(@RequestBody CourseRequestDTO dto) {
        return ResponseEntity.ok(courseService.create(dto));
    }

    // ======= Cập nhật khóa học (ADMIN) =======
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật khóa học", description = "Chỉ ADMIN được phép cập nhật khóa học")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    public ResponseEntity<CourseResponseDTO> updateCourse(
            @Parameter(description = "Mã khóa học") @PathVariable Long id,
            @RequestBody CourseRequestDTO dto) {
        return ResponseEntity.ok(courseService.update(id, dto));
    }

    // ======= Xóa khóa học (ADMIN) =======
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa khóa học", description = "Chỉ ADMIN được phép xóa khóa học")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    public ResponseEntity<Void> deleteCourse(@Parameter(description = "Mã khóa học") @PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ======= Lấy khóa học theo ID (ADMIN + USER) =======
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Lấy chi tiết khóa học", description = "Trả về thông tin khóa học theo ID")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<CourseResponseDTO> getCourse(@Parameter(description = "Mã khóa học") @PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    // ======= Lấy tất cả khóa học (ADMIN + USER) =======
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Danh sách khóa học", description = "Trả về tất cả khóa học")
    @ApiResponse(responseCode = "200", description = "Thành công")
    public ResponseEntity<List<CourseResponseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.findAll());
    }
}
