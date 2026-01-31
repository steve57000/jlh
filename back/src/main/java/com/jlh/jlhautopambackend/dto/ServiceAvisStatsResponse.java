package com.jlh.jlhautopambackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAvisStatsResponse {
    private Integer serviceId;
    private Double moyenneNote;
    private Long totalAvis;
}
