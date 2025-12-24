package com.ra.base_spring_boot.dto.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PostResponseDTO {
    private Long id;
    private String title;
    private String slug;
    private String content;
    private AuthorResponse author;
    private String status;
    private LocalDateTime createdAt;
    private List<String> tags;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor

    public static class AuthorResponse {
        private Long id;
        private String fullName;

    }
}
