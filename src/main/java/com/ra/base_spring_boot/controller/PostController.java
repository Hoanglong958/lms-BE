package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.Post.PostRequestDTO;
import com.ra.base_spring_boot.dto.Post.PostResponseDTO;
import com.ra.base_spring_boot.services.post.IPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "22 - Posts", description = "Quản lý bài viết")
public class PostController {

    private final IPostService postService;

    // ================== CREATE POST (ADMIN) ==================
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Tạo bài viết", description = "Chỉ ADMIN được tạo bài viết")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponseDTO.class), examples = @ExampleObject(name = "CreatedPost", value = "{\n  \"id\": 1,\n  \"title\": \"Spring Boot Tips\",\n  \"slug\": \"spring-boot-tips\",\n  \"imageUrl\": \"https://example.com/image.jpg\",\n  \"content\": \"Nội dung bài viết...\",\n  \"status\": \"PUBLISHED\",\n  \"createdAt\": \"2025-11-27T14:00:00\",\n  \"author\": {\n    \"id\": 1,\n    \"fullName\": \"Nguyen Van A\"\n  },\n  \"tags\": [\"Spring\", \"Java\"]\n}"))),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content)
    })
    public ResponseEntity<PostResponseDTO> createPost(@RequestBody PostRequestDTO dto) {
        PostResponseDTO response = postService.createPost(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ================== UPDATE POST (ADMIN) ==================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Cập nhật bài viết", description = "Chỉ ADMIN được cập nhật bài viết")
    public ResponseEntity<PostResponseDTO> updatePost(
            @Parameter(description = "ID bài viết") @PathVariable Long id,
            @RequestBody PostRequestDTO dto) {
        PostResponseDTO response = postService.updatePost(id, dto);
        return ResponseEntity.ok(response);
    }

    // ================== DELETE POST (ADMIN) ==================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Xóa bài viết", description = "Chỉ ADMIN được xóa bài viết")
    public ResponseEntity<Void> deletePost(@Parameter(description = "ID bài viết") @PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // ================== GET POST BY ID ==================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'TEACHER')")
    @Operation(summary = "Lấy chi tiết bài viết", description = "ADMIN + USER có thể xem bài viết")
    public ResponseEntity<PostResponseDTO> getPostById(@Parameter(description = "ID bài viết") @PathVariable Long id) {
        PostResponseDTO response = postService.getPostById(id);
        return ResponseEntity.ok(response);
    }

    // ================== GET PUBLISHED POSTS (PAGE) ==================
    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Danh sách bài viết đã xuất bản", description = "Danh sách bài viết PUBLISHED, phân trang + tìm kiếm")
    public ResponseEntity<Page<PostResponseDTO>> getPublishedPosts(
            @Parameter(description = "Từ khóa tìm kiếm theo tiêu đề") @RequestParam(value = "q", required = false) String q,
            @Parameter(description = "Tên tag lọc") @RequestParam(value = "tag", required = false) String tag,
            @Parameter(description = "Trạng thái (PUBLISHED, DRAFT, ARCHIVED, ALL)") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Trang bắt đầu từ 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp, ví dụ: createdAt,desc hoặc title,asc") @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Page<PostResponseDTO> result = postService.searchPostsAdvanced(q, tag, status, page, size, sort);
        return ResponseEntity.ok(result);
    }

    // ================== GET ALL TAGS ==================
    @GetMapping("/tags")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'TEACHER')")
    @Operation(summary = "Lấy danh sách tất cả các tags")
    public ResponseEntity<List<String>> getAllTags() {
        return ResponseEntity.ok(postService.getAllTags());
    }

    // ================== GET DRAFT POSTS (ADMIN) ==================
    @GetMapping("/drafts")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Danh sách bài viết bản nháp", description = "Chỉ ADMIN xem được danh sách bài viết DRAFT, phân trang")
    public ResponseEntity<Page<PostResponseDTO>> getDraftPosts(
            @Parameter(description = "Trang bắt đầu từ 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp, ví dụ: createdAt,desc hoặc title,asc") @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Sort sortObj;
        String[] sortParts = sort.split(",");
        if (sortParts.length == 2) {
            sortObj = Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<PostResponseDTO> posts = postService.getDraftPosts(pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(posts);
    }
}
