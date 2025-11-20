package com.ra.base_spring_boot.dto.DashBoardStats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGrowthPointDTO {
    private String period; // e.g. "2025-11" or "2025-W45" or "2025-11-07"
    private long count;
}
