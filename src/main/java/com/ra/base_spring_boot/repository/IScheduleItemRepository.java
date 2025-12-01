package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    List<ScheduleItem> findByCourseIdOrderBySessionNumber(Long courseId);
    void deleteByCourseId(Long courseId);
}