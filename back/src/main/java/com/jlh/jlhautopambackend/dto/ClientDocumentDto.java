package com.jlh.jlhautopambackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDocumentDto {
    private Integer demandeId;
    private Instant dateDemande;
    private TypeDemandeDto typeDemande;
    private StatutDemandeDto statutDemande;
    private RendezVousResponse rendezVous;
    private DemandeDocumentDto document;
}
