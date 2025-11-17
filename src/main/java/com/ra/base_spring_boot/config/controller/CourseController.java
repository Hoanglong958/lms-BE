package com.ra.base_spring_boot.config.controller;

import com.ra.base_spring_boot.dto.Course.CourseResponseDTO;
import com.ra.base_spring_boot.dto.Course.CourseRequestDTO;
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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponseDTO.class),
                            examples = @ExampleObject(name = "CreatedCourse", value = "{\n  \"id\": 1,\n  \"title\": \"Spring Boot Fundamentals\",\n  \"description\": \"Learn how to build REST APIs with Spring Boot 3\",\n  \"instructorName\": \"Nguyen Van A\",\n  \"level\": \"BEGINNER\",\n  \"createdAt\": \"2025-11-14T09:30:00\"\n}"))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<CourseResponseDTO> createCourse(@RequestBody CourseRequestDTO dto) {
        return ResponseEntity.ok(courseService.create(dto));
    }

    // ======= Cập nhật khóa học (ADMIN) =======
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cập nhật khóa học", description = "Chỉ ADMIN được phép cập nhật khóa học")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponseDTO.class),
                            examples = @ExampleObject(name = "UpdatedCourse", value = "{\n  \"id\": 1,\n  \"title\": \"Spring Boot Advanced\",\n  \"description\": \"Deep dive into Spring Boot internals\",\n  \"instructorName\": \"Nguyen Van A\",\n  \"level\": \"INTERMEDIATE\",\n  \"createdAt\": \"2025-11-14T10:00:00\"\n}"))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<CourseResponseDTO> updateCourse(
            @Parameter(description = "Mã khóa học") @PathVariable Long id,
            @RequestBody CourseRequestDTO dto) {
        return ResponseEntity.ok(courseService.update(id, dto));
    }

    // ======= Xóa khóa học (ADMIN) =======
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xóa khóa học", description = "Chỉ ADMIN được phép xóa khóa học")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<Void> deleteCourse(@Parameter(description = "Mã khóa học") @PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ======= Lấy khóa học theo ID (ADMIN + USER) =======
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Lấy chi tiết khóa học", description = "Trả về thông tin khóa học theo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponseDTO.class),
                            examples = @ExampleObject(name = "CourseDetail", value = "{\n  \"id\": 1,\n  \"title\": \"Spring Boot Fundamentals\",\n  \"description\": \"Learn how to build REST APIs with Spring Boot 3\",\n  \"instructorName\": \"Nguyen Van A\",\n  \"level\": \"BEGINNER\",\n  \"createdAt\": \"2025-11-14T09:30:00\"\n}"))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<CourseResponseDTO> getCourse(@Parameter(description = "Mã khóa học") @PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    // ======= Lấy tất cả khóa học (ADMIN + USER) =======
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Danh sách khóa học", description = "Trả về tất cả khóa học")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CourseResponseDTO.class)),
                            examples = @ExampleObject(name = "CourseList", value = "[{\n  \"id\": 1,\n  \"title\": \"Spring Boot Fundamentals\",\n  \"description\": \"Learn how to build REST APIs with Spring Boot 3\",\n  \"instructorName\": \"Nguyen Van A\",\n  \"level\": \"BEGINNER\",\n  \"createdAt\": \"2025-11-14T09:30:00\"\n}]"))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<List<CourseResponseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.findAll());
    }

    // ======= Danh sách khóa học có phân trang + tìm kiếm (ADMIN + USER) =======
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Danh sách khóa học (phân trang)", description = "Phân trang + tìm kiếm theo tiêu đề hoặc giảng viên")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponseDTO.class),
                            examples = @ExampleObject(name = "PagedCourse", value = "{\n  \"content\": [{\n    \"id\": 1,\n    \"title\": \"Spring Boot Fundamentals\",\n    \"description\": \"Learn how to build REST APIs with Spring Boot 3\",\n    \"instructorName\": \"Nguyen Van A\",\n    \"level\": \"BEGINNER\",\n    \"createdAt\": \"2025-11-14T09:30:00\"\n  }],\n  \"pageable\": {\n    \"pageNumber\": 0,\n    \"pageSize\": 10\n  },\n  \"totalElements\": 1,\n  \"totalPages\": 1,\n  \"first\": true,\n  \"last\": true\n}"))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống", content = @Content)
    })
    public ResponseEntity<Page<CourseResponseDTO>> getCoursesPaging(
            @Parameter(description = "Từ khóa tìm kiếm theo title hoặc instructorName")
            @RequestParam(value = "q", required = false) String q,
            @Parameter(description = "Trang bắt đầu từ 0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp, ví dụ: createdAt,desc hoặc title,asc")
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {

        Sort sortObj;
        String[] sortParts = sort.split(",");
        if (sortParts.length == 2) {
            sortObj = Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<CourseResponseDTO> result = (q != null && !q.trim().isEmpty())
                ? courseService.search(q, pageable)
                : courseService.findAll(pageable);

        return ResponseEntity.ok(result);
    }
}
