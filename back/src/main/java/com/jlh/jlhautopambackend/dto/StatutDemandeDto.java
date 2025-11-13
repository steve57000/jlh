package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutDemandeDto {
    /** Code du statut de la demande */
    private String codeStatut;
    private String libelle;
}
