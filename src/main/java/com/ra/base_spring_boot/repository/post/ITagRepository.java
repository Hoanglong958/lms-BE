package com.ra.base_spring_boot.repository.post;

import com.ra.base_spring_boot.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITagRepository extends JpaRepository<Tag, Long> {
    Tag findByName(String name);
}

