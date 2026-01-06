package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeServiceResponse {
    private DemandeServiceKeyDto id;
    private Integer quantite;
    private String libelle;
    private String description;
    private java.math.BigDecimal prixUnitaire;
    private Integer rendezVousId;
}
