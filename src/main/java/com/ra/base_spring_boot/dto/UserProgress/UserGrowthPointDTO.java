package com.ra.base_spring_boot.dto.UserProgress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGrowthPointDTO {
    private String month; // Ví dụ "2025-11"
    private long userCount;
    private double growthRate; // phần trăm tăng trưởng so với tháng trước

}
