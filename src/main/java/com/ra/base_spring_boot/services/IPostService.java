package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Post.PostRequestDTO;
import com.ra.base_spring_boot.dto.Post.PostResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;

public interface IPostService {

    // Create bài viết mới
    PostResponseDTO createPost(PostRequestDTO request);

    // Lấy danh sách bài viết đã xuất bản (phân trang)
    Page<PostResponseDTO> getPublishedPosts(int page, int size);

    // Lấy danh sách bài viết bản nháp (phân trang)
    Page<PostResponseDTO> getDraftPosts(int page, int size);

    // Lấy chi tiết bài viết theo ID
    PostResponseDTO getPostById(Long id);

    // Update bài viết theo ID
    PostResponseDTO updatePost(Long id, PostRequestDTO request);

    // Delete bài viết theo ID
    void deletePost(Long id);

    // Tìm kiếm bài viết (PUBLISHED)
    Page<PostResponseDTO> searchPosts(String q, int page, int size);

    // Tìm kiếm bài viết nâng cao
    Page<PostResponseDTO> searchPostsAdvanced(String q, String tagName, String status, int page, int size, String sort);

    // Lấy tất cả các tags
    List<String> getAllTags();
}
