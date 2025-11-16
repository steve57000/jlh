package com.jlh.jlhautopambackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class DemandeServiceDto {
    private Integer idDemande;
    private Integer idService;
    private Integer quantite;
    private String libelleService;
    private String descriptionService;
    private BigDecimal prixUnitaireService;
    private Integer quantiteMax;
    private String privateNoteService;
    private Instant dateHeureService;
}
