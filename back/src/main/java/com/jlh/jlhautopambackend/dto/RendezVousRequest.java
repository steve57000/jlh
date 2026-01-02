package com.jlh.jlhautopambackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousRequest {

    /** Id de la demande (en Brouillon au moment de la création du RDV). */
    @NotNull
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
}
