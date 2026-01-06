package com.jlh.jlhautopambackend.dto;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DemandeResponse {
    private Integer idDemande;
    private Instant dateDemande;

    // AU LIEU DE clientId -> on expose directement les infos utiles
    private ClientSummaryDto client;

    // Déjà présents chez toi
    private TypeDemandeDto typeDemande;       // codeType + libelle
    private StatutDemandeDto statutDemande;   // codeStatut + libelle

    // AU LIEU de clés {idDemande,idService} seulement -> on envoie les infos affichables
    private List<DemandeServiceDto> services;

    private List<DemandeDocumentDto> documents;

    private List<DemandeTimelineEntryDto> timeline;

    private DevisResponse devis;

    private RendezVousResponse rendezVous;
}
