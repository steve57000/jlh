package com.jlh.jlhautopambackend.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * DTO de réponse pour Creneau, expose uniquement les champs utiles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreneauResponse {
    private Integer idCreneau;
    private Instant dateDebut;
    private Instant dateFin;
    private StatutCreneauDto statut;
    /**
     * Liste des disponibilités (uniquement leurs IDs composites).
     * Réutilise le DTO déjà défini.
     */
    private List<DisponibiliteIdDto> disponibilites;
}
