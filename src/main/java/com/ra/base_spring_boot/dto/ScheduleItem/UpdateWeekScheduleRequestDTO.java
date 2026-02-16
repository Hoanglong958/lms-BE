package com.ra.base_spring_boot.dto.ScheduleItem;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateWeekScheduleRequestDTO {
    
    @NotNull(message = "ClassCourse ID không được để trống")
    private Long classCourseId;
    
    @NotNull(message = "Ngày bắt đầu tuần không được để trống")
    private LocalDate weekStartDate;
    
    @NotNull(message = "Ngày kết thúc tuần không được để trống")
    private LocalDate weekEndDate;
    
    private List<WeekScheduleItem> scheduleItems;
    
    @Data
    public static class WeekScheduleItem {
        private Long scheduleItemId; // null nếu tạo mới
        private Integer dayIndex; // 0-6 (Monday=0)
        private Integer periodId;
        private LocalDate date;
    }
}
