package com.ra.base_spring_boot.dto.Post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequestDTO {
    private String title;
    private String slug;
    private String content;

    @Schema(example = "https://example.com/course-image.jpg")
    private String imageUrl;

    private Long authorId; // id của user tạo bài
    private List<String> tagNames; // danh sách tên tag
    private String status; // DRAFT hoặc PUBLISHED

}