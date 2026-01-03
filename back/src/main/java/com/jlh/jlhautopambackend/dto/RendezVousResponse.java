package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousResponse {
    /** Identifiant du rendez-vous */
    private Integer idRdv;
    /** Identifiant de la demande */
    private Integer demandeId;
    /** Identifiant du créneau */
    private Integer creneauId;
    /** Identifiant de l’administrateur */
    private Integer administrateurId;
    /** Statut du rendez-vous */
    private StatutRendezVousDto statut;
}