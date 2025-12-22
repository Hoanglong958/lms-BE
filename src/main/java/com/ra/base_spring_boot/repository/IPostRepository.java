package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Post;
import com.ra.base_spring_boot.model.constants.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IPostRepository extends JpaRepository<Post, Long> {

    // ✅ LIST bài viết theo status (có tags)
    @Query(
            value = """
            SELECT DISTINCT p
            FROM Post p
            LEFT JOIN FETCH p.tags
            WHERE p.status = :status
        """,
            countQuery = """
            SELECT COUNT(p)
            FROM Post p
            WHERE p.status = :status
        """
    )
    Page<Post> findByStatusWithTags(
            @Param("status") PostStatus status,
            Pageable pageable
    );

    // ✅ CHI TIẾT bài viết theo ID (có tags)
    @Query("""
        SELECT DISTINCT p
        FROM Post p
        LEFT JOIN FETCH p.tags
        WHERE p.id = :id
    """)
    Optional<Post> findPostDetailById(@Param("id") Long id);
}
