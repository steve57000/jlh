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
    private com.jlh.jlhautopambackend.modeles.ServiceQuantiteMode quantiteMode;
    private com.jlh.jlhautopambackend.modeles.ServicePrixMode prixMode;
    private Integer tailleLot;
    private Integer rendezVousId;
}
