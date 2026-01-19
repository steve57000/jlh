package com.jlh.jlhautopambackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardTypeStatDto {
    private String type;
    private String label;
    private long count;
    private int percentage;
    private double averageValue;
}
