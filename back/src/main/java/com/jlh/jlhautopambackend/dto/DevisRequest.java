package com.jlh.jlhautopambackend.dto;

import lombok.*;
import java.time.Instant;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevisRequest {
    /** Identifiant de la demande liée au devis */
    private Integer demandeId;
    /** Date d'émission du devis */
    private Instant dateDevis;
    /** Montant total du devis */
    private BigDecimal montantTotal;
}