package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.ScheduleItem.*;

import java.util.List;

public interface IScheduleItemService {

    List<ScheduleItemResponseDTO> generateScheduleForCourse(GenerateScheduleRequestDTO req);

    List<ScheduleItemResponseDTO> createManualSchedule(CreateManualScheduleRequestDTO req);

    ClassScheduleResponseDTO getScheduleByClassCourse(Long classCourseId);

    void clearScheduleForCourse(Long classCourseId);

    ScheduleItemResponseDTO updateScheduleItem(Long id, UpdateScheduleItemRequestDTO req);
    List<ScheduleItemResponseDTO> getScheduleByCourse(Long courseId);
}
