package com.ra.base_spring_boot.dto.ScheduleItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GenerateScheduleRequestDTO {
    private Long courseId;
    // Nếu muốn chỉ dùng 1 tập period (chuỗi id) để lập lịch; nếu null -> lấy tất cả period liên quan
    private List<Long> periodIds;
}