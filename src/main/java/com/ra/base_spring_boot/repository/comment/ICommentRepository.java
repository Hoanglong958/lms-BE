package com.ra.base_spring_boot.repository.comment;

import com.ra.base_spring_boot.model.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = { "author" })
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
