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
public class DemandeDocumentDownload {
    private Long idDocument;
    private Integer demandeId;
    private String nomFichier;
    private String urlPrivate;   // chemin relatif sur disque (ex: documents/uuid.pdf)
    private String typeContenu;
    private Long tailleOctets;
    private boolean visibleClient;
    private String creePar;
    private String creeParRole;
    private Instant creeLe;
}
