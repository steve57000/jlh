package com.jlh.jlhautopambackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardAnalyticsDto {
    private long totalDemandes;
    private long pending;
    private long traitees;
    private long annulees;
    private long devisTotal;
    private long devisAvecRdv;
    private long devisSansSuite;
    private BigDecimal revenueTotal;
    private List<AdminDashboardTypeStatDto> typeStats;
    private List<AdminDashboardServiceStatDto> serviceStats;
    private List<AdminDashboardRevenueStatDto> revenueStats;
    private List<AdminYearlyStatsDto> yearly;
}
