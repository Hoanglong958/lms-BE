package com.ra.base_spring_boot.repository.classroom;

import com.ra.base_spring_boot.model.ScheduleItemContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IScheduleItemContentRepository extends JpaRepository<ScheduleItemContent, Long> {
    void deleteByAssignment_Id(Long assignmentId);

    void deleteByAssignment_IdAndScheduleItem_IdIn(Long assignmentId, List<Long> scheduleItemIds);

    List<ScheduleItemContent> findByAssignment_IdOrderByOrderIndexAsc(Long assignmentId);
}
