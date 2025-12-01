package com.ra.base_spring_boot.dto.Period;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class PeriodRequestDTO {

    @NotBlank(message = "Tên ca học không được để trống")
    private String name;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalTime endTime;
    @NotNull(message = "Ngày trong tuần không được để trống")
    private Integer dayOfWeek; // 1 = Monday, 7 = Sunday


}
