package com.ra.base_spring_boot.services.comment;

import com.ra.base_spring_boot.dto.Comment.CommentRequestDTO;
import com.ra.base_spring_boot.dto.Comment.CommentResponseDTO;
import com.ra.base_spring_boot.model.User;

import java.util.List;

public interface ICommentService {

    List<CommentResponseDTO> getCommentsByPost(Long postId);

    CommentResponseDTO addComment(Long postId, CommentRequestDTO request, User author);
}
