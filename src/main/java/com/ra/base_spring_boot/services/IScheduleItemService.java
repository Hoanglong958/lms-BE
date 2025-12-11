package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.ScheduleItem.CreateManualScheduleRequestDTO;
import com.ra.base_spring_boot.dto.ScheduleItem.ScheduleItemResponseDTO;
import com.ra.base_spring_boot.dto.ScheduleItem.GenerateScheduleRequestDTO;
import com.ra.base_spring_boot.dto.ScheduleItem.UpdateScheduleItemRequestDTO;

import java.util.List;

public interface IScheduleItemService {

    // Tạo thời khóa biểu cho khóa học
    List<ScheduleItemResponseDTO> generateScheduleForCourse(GenerateScheduleRequestDTO req);

    // Lấy danh sách thời khóa biểu theo course
    List<ScheduleItemResponseDTO> getScheduleByCourse(Long courseId);

    // Xóa thời khóa biểu của một khóa học
    void clearScheduleForCourse(Long courseId);

    // Cập nhật từng buổi học
    ScheduleItemResponseDTO updateScheduleItem(Long scheduleItemId, UpdateScheduleItemRequestDTO req);
    List<ScheduleItemResponseDTO> createManualSchedule(CreateManualScheduleRequestDTO req);

}
