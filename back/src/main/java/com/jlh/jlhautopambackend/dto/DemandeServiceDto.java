package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeServiceDto {
    private Integer idService;
    private String libelle;
    private String description;
    private Integer quantite;
    private java.math.BigDecimal prixUnitaire;
    private Integer quantiteMax;
}
