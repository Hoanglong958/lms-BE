package com.ra.base_spring_boot.dto.DashBoardStats;

import com.ra.base_spring_boot.model.constants.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RecentTransactionDTO {
    private String id;
    private String user;
    private String course;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime time;
}
