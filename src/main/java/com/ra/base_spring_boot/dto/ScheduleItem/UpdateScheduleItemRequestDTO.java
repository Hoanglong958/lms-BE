package com.ra.base_spring_boot.dto.ScheduleItem;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateScheduleItemRequestDTO {
    private LocalDate date;          // Ngày mới
    private Long periodId;           // Ca học mới
    private String status;           // "SCHEDULED", "CANCELLED", ...
    private Long classId;
}
