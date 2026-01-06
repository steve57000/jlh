package com.jlh.jlhautopambackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousRequest {

    /** Id de la demande (optionnel pour les rendez-vous libres). */
    private Integer demandeId;

    /** Id du créneau choisi par le client (matin/après-midi ⇒ tu mappes vers un Creneau en base). */
    @NotNull
    private Integer creneauId;

    /** Id de l’administrateur « propriétaire » du RDV (si nécessaire). */
    @NotNull
    private Integer administrateurId;

    /** Code du statut RDV initial (ex: "Confirme", "Reporte", "Annule"). */
    @NotNull
    private String codeStatut;

    /** Identifiant du client (utile pour un admin qui crée un rendez-vous). */
    private Integer clientId;

    /** Commentaire associé au rendez-vous. */
    private String commentaire;
}
