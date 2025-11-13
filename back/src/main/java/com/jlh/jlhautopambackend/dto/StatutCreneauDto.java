package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutCreneauDto {
    /** Le code (String) du statut, c'est la PK de StatutCreneau */
    private String codeStatut;
    private String libelle;
}
