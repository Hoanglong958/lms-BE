package com.ra.base_spring_boot.services.comment.impl;

import com.ra.base_spring_boot.dto.Comment.CommentRequestDTO;
import com.ra.base_spring_boot.dto.Comment.CommentResponseDTO;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Comment;
import com.ra.base_spring_boot.model.Post;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.PostStatus;
import com.ra.base_spring_boot.repository.comment.ICommentRepository;
import com.ra.base_spring_boot.repository.post.IPostRepository;
import com.ra.base_spring_boot.services.comment.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final ICommentRepository commentRepository;
    private final IPostRepository postRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByPost(Long postId) {
        ensurePostExists(postId);
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponseDTO addComment(Long postId, CommentRequestDTO request, User author) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new HttpBadRequest("Nội dung bình luận không được để trống");
        }

        if (author == null) {
            throw new HttpBadRequest("Tài khoản người dùng không hợp lệ");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new HttpBadRequest("Post không tồn tại"));

        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new HttpBadRequest("Chỉ có thể bình luận trên bài viết đã xuất bản");
        }

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(request.getContent().trim())
                .build();

        return mapToDto(commentRepository.save(comment));
    }

    private void ensurePostExists(Long postId) {
        if (postId == null || !postRepository.existsById(postId)) {
            throw new HttpBadRequest("Post không tồn tại");
        }
    }

    private CommentResponseDTO mapToDto(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .author(CommentResponseDTO.AuthorResponse.builder()
                        .id(comment.getAuthor().getId())
                        .fullName(comment.getAuthor().getFullName())
                        .avatar(comment.getAuthor().getAvatar())
                        .build())
                .build();
    }
}
