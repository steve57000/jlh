package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutRendezVousDto {
    /** Code du statut de rendez-vous */
    private String codeStatut;
    private String libelle;
}
