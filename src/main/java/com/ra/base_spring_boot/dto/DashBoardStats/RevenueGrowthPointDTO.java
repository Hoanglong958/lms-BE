package com.ra.base_spring_boot.dto.DashBoardStats;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RevenueGrowthPointDTO {
    private String period; // e.g. "T1", "T2" or "2025-01"
    private BigDecimal revenue;
}
