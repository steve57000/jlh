package com.jlh.jlhautopambackend.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminYearlyStatsDto {
    private int year;
    private long serviceCount;
    private BigDecimal serviceRevenue;
    private long devisCount;
    private BigDecimal devisRevenue;
    private long rendezVousCount;
    private boolean forecast;
}
