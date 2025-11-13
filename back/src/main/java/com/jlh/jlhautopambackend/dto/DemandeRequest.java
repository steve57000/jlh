package com.jlh.jlhautopambackend.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeRequest {
    private Instant dateDemande;
    private Integer clientId;
    /** Code du type de demande (clé primaire de TypeDemande) */
    private String codeType;
    /** Code du statut de la demande (clé primaire de StatutDemande) */
    private String codeStatut;
}
