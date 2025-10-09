package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.CourseCreateReq;
import com.ra.base_spring_boot.dto.req.CourseUpdateReq;
import com.ra.base_spring_boot.services.ICourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CourseController(Khóa Học)", description = "quản lý khoá học (CRUD + tìm kiếm phân trang)")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final ICourseService courseService;

    @Operation(summary = "tạo khoá học")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CourseCreateReq req) {
        var data = courseService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseWrapper.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data(data)
                        .build()
        );
    }

    @Operation(summary = "lấy chi tiết khoá học")
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Integer id) {
        var data = courseService.get(id);
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(data)
                        .build()
        );
    }

    @Operation(summary = "tìm kiếm khoá học có phân trang")
    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        var data = courseService.search(keyword, pageable);
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(data)
                        .build()
        );
    }

    @Operation(summary = "cập nhật khoá học")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody CourseUpdateReq req) {
        var data = courseService.update(id, req);
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(data)
                        .build()
        );
    }

    @Operation(summary = "xóa khoá học")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        courseService.delete(id);
        return ResponseEntity.ok(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Deleted")
                        .build()
        );
    }
}
