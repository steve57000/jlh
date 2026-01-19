package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.AdminDashboardAnalyticsDto;
import com.jlh.jlhautopambackend.dto.AdminDashboardStatsDto;

public interface AdminDashboardStatsService {
    AdminDashboardStatsDto getStats();

    AdminDashboardAnalyticsDto getAnalytics(AdminDashboardAnalyticsFilter filter);

    record AdminDashboardAnalyticsFilter(
            java.time.Instant from,
            java.time.Instant to,
            java.util.List<String> types,
            java.util.List<String> statuts,
            java.util.List<Integer> serviceIds,
            boolean includeForecast
    ) {}
}
