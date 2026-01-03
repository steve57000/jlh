package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeDemandeDto {
    /** Code du type de demande */
    private String codeType;
    private String libelle;
}
