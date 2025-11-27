package com.ra.base_spring_boot.repository;
import com.ra.base_spring_boot.model.Post;
import com.ra.base_spring_boot.model.constants.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByStatus(PostStatus status, Pageable pageable);
}

