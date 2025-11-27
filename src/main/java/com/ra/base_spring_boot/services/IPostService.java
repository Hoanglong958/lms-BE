package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.Post.PostRequestDTO;
import com.ra.base_spring_boot.dto.Post.PostResponseDTO;
import org.springframework.data.domain.Page;

public interface IPostService {

    // Create bài viết mới
    PostResponseDTO createPost(PostRequestDTO request);

    // Lấy danh sách bài viết đã xuất bản (phân trang)
    Page<PostResponseDTO> getPublishedPosts(int page, int size);

    // Lấy chi tiết bài viết theo ID
    PostResponseDTO getPostById(Long id);

    // Update bài viết theo ID
    PostResponseDTO updatePost(Long id, PostRequestDTO request);

    // Delete bài viết theo ID
    void deletePost(Long id);
}
