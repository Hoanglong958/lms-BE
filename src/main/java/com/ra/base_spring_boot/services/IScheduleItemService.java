package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.ScheduleItem.*;

import java.util.List;
import java.time.LocalDate;

public interface IScheduleItemService {

    // Tạo thời khóa biểu cho khóa học
    List<ScheduleItemResponseDTO> generateScheduleForCourse(GenerateScheduleRequestDTO req);

    // Lấy danh sách thời khóa biểu theo course
    List<ScheduleItemResponseDTO> getScheduleByCourse(Long courseId);

    // Lấy danh sách TKB theo course và class
    List<ScheduleItemResponseDTO> getScheduleByCourseAndClass(Long courseId, Long classId);

    // Lấy TKB theo course + class với bộ lọc tùy chọn
    List<ScheduleItemResponseDTO> getScheduleByCourseAndClassFiltered(Long courseId, Long classId, String status, LocalDate from, LocalDate to, Long periodId);

    // Xóa thời khóa biểu của một khóa học
    void clearScheduleForCourse(Long courseId);

    // Cập nhật từng buổi học
    ScheduleItemResponseDTO updateScheduleItem(Long scheduleItemId, UpdateScheduleItemRequestDTO req);
    List<ScheduleItemResponseDTO> createManualSchedule(CreateManualScheduleRequestDTO req);
    ClassScheduleResponseDTO getScheduleByClassCourse(Long classCourseId);


}
