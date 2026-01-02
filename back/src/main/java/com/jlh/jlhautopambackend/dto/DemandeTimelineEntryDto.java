package com.jlh.jlhautopambackend.dto;

import com.jlh.jlhautopambackend.modeles.DemandeTimelineType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeTimelineEntryDto {
    private Long id;
    private DemandeTimelineType type;
    private Instant createdAt;
    private String createdBy;
    private String createdByRole;
    private boolean visibleClient;
    private StatutDemandeDto statut;
    private String commentaire;
    private BigDecimal montantValide;
    private DemandeDocumentDto document;
    private RendezVousTimelineDto rendezVous;
    private String source;
}
