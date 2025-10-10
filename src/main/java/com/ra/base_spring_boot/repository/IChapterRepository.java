package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findByCourse_Id(Long courseId);
}
