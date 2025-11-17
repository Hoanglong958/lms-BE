package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Classroom.ClassStatsResponseDTO;
import com.ra.base_spring_boot.dto.Classroom.ClassroomRequestDTO;
import com.ra.base_spring_boot.dto.Classroom.ClassroomResponseDTO;
import com.ra.base_spring_boot.services.IClassroomService;
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

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Quản lý lớp học, học viên, giảng viên và khóa học")
public class ClassroomController {

    private final IClassroomService classroomService;

    // =========== CRUD lớp học ===========
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Tạo lớp học mới")
    public ResponseEntity<ClassroomResponseDTO> create(@RequestBody ClassroomRequestDTO dto) {
        return ResponseEntity.ok(classroomService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật lớp học")
    public ResponseEntity<ClassroomResponseDTO> update(@PathVariable Long id,
                                                       @RequestBody ClassroomRequestDTO dto) {
        return ResponseEntity.ok(classroomService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa lớp học")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classroomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Chi tiết lớp học")
    public ResponseEntity<ClassroomResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Danh sách lớp học")
    public ResponseEntity<List<ClassroomResponseDTO>> findAll() {
        return ResponseEntity.ok(classroomService.findAll());
    }

    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Danh sách lớp học (phân trang)")
    public ResponseEntity<Page<ClassroomResponseDTO>> paging(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        Sort sortObj;
        if (sortParts.length == 2) {
            sortObj = Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<ClassroomResponseDTO> result = (keyword != null && !keyword.isBlank())
                ? classroomService.search(keyword, pageable)
                : classroomService.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    // =========== Thống kê lớp học ===========
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    @Operation(summary = "Thống kê các chỉ số trong lớp học",
            description = "Trả về sĩ số, số HV active/completed/dropped, điểm trung bình và tỉ lệ điểm danh")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thống kê lớp học",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClassStatsResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy lớp học", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<ClassStatsResponseDTO> getClassStats(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.getClassStats(id));
    }
}
