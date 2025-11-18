package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.config.dto.Statistic.DashboardStatsDTO;

public interface IDashboardService {
    DashboardStatsDTO getDashboard();
}
