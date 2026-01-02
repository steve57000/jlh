package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibiliteRequest {
    private Integer idAdmin;
    private Integer idCreneau;
}