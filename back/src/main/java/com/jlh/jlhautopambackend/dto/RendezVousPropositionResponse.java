package com.jlh.jlhautopambackend.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousPropositionResponse {
    private Integer idProposition;
    private Instant dateDebut;
    private Instant dateFin;
    private String statut;
    private Instant createdAt;
    private Instant expiresAt;
    private Integer administrateurId;
    private String administrateurNom;
}
