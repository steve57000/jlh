package com.jlh.jlhautopambackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousRequest {

    /** Id de la demande (optionnel pour les rendez-vous libres). */
    private Integer demandeId;

    /** Id du créneau choisi par le client (matin/après-midi ⇒ tu mappes vers un Creneau en base). */
    private Integer creneauId;

    /** Date de début du créneau (optionnel si creneauId est fourni). */
    private Instant dateDebut;

    /** Date de fin du créneau (optionnel si creneauId est fourni). */
    private Instant dateFin;

    /** Id de l’administrateur « propriétaire » du RDV (si nécessaire). */
    private Integer administrateurId;

    /** Code du statut RDV initial (ex: "Confirme", "Reporte", "Annule"). */
    @NotNull
    private String codeStatut;

    /** Identifiant du client (utile pour un admin qui crée un rendez-vous). */
    private Integer clientId;

    /** Commentaire associé au rendez-vous. */
    private String commentaire;
}
