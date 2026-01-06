package com.jlh.jlhautopambackend.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousResponse {
    /** Identifiant du rendez-vous */
    private Integer idRdv;
    /** Identifiant de la demande */
    private Integer demandeId;
    /** Identifiant du service associé (si RDV lié à un service) */
    private Integer serviceId;
    /** Identifiant du devis associé (si RDV lié à un devis) */
    private Integer devisId;
    /** Identifiant du client */
    private Integer clientId;
    /** Identifiant du créneau */
    private Integer creneauId;
    /** Date de début du rendez-vous */
    private Instant dateDebut;
    /** Date de fin du rendez-vous */
    private Instant dateFin;
    /** Identifiant de l’administrateur */
    private Integer administrateurId;
    /** Statut du rendez-vous */
    private StatutRendezVousDto statut;
    /** Commentaire */
    private String commentaire;
}
