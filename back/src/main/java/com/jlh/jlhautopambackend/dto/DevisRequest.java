package com.jlh.jlhautopambackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevisRequest {

    private Integer demandeId;

    private Instant dateDevis;

    /** Prix total global du devis (main d'œuvre + pièces) */
    private BigDecimal montantTotal;

    /** Montant de la main d'œuvre */
    private BigDecimal montantMainOeuvre;

    /** Montant des pièces */
    private BigDecimal montantPieces;

    private Integer rendezVousId;
}
