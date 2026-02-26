package com.ra.base_spring_boot.services.classroom;

import com.ra.base_spring_boot.dto.ScheduleItem.*;

import java.time.LocalDate;
import java.util.List;

public interface IScheduleItemService {

    List<ScheduleItemResponseDTO> generateScheduleForCourse(GenerateScheduleRequestDTO req);

    List<ScheduleItemResponseDTO> createManualSchedule(CreateManualScheduleRequestDTO req);

    ClassScheduleResponseDTO getScheduleByClassCourse(Long classCourseId);

    void clearScheduleForCourse(Long classCourseId);

    ScheduleItemResponseDTO updateScheduleItem(Long id, UpdateScheduleItemRequestDTO req);

    List<ScheduleItemResponseDTO> getScheduleByCourse(Long courseId);

    // =========================================================
    // 6. UPDATE WEEK SCHEDULE - Cập nhật lịch theo tuần cụ thể
    // =========================================================
    List<ScheduleItemResponseDTO> updateWeekSchedule(UpdateWeekScheduleRequestDTO req);

    // =========================================================
    // 7. GET SCHEDULED DATES FOR A CLASS BY YEAR/MONTH
    // =========================================================
    List<LocalDate> getScheduledDatesForClass(Long classId, int year, int month);
}
