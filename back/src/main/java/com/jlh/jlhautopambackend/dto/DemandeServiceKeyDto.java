package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeServiceKeyDto {
    private Integer idDemande;
    private Integer idService;
}
