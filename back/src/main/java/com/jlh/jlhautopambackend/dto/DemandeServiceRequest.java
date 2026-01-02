package com.jlh.jlhautopambackend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeServiceRequest {
    private Integer demandeId;
    private Integer serviceId;
    private Integer quantite;
    @JsonAlias("prixUnitaireService")
    private BigDecimal prixUnitaire;
}
