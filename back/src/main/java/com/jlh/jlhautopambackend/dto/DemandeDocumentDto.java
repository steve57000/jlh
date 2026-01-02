package com.jlh.jlhautopambackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeDocumentDto {

    private Long idDocument;

    private String nomFichier;

    private String typeContenu;

    private Long tailleOctets;

    private boolean visibleClient;

    private String creePar;

    private String creeParRole;

    private Instant creeLe;

    private String downloadUrl;
}
