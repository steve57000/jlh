package com.jlh.jlhautopambackend.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreneauRequest {
    private Instant dateDebut;
    private Instant dateFin;

    /**
     * Code du statut à appliquer à ce créneau (String, clé primaire de StatutCreneau).
     */
    private String codeStatut;
}
