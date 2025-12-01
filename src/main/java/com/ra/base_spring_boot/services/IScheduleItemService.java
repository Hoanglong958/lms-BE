package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.ScheduleItem.ScheduleItemResponseDTO;
import com.ra.base_spring_boot.dto.ScheduleItem.GenerateScheduleRequestDTO;

import java.util.List;

public interface IScheduleItemService {
    List<ScheduleItemResponseDTO> generateScheduleForCourse(GenerateScheduleRequestDTO req);
    List<ScheduleItemResponseDTO> getScheduleByCourse(Long courseId);
    void clearScheduleForCourse(Long courseId);
}
