package com.jlh.jlhautopambackend.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisServiceResponse {
    private Long idAvis;
    private Integer demandeId;
    private Integer serviceId;
    private String serviceLibelle;
    private Integer clientId;
    private String clientNomPrenom;
    private Integer note;
    private String commentaire;
    private Instant creeLe;
    private String statut;
    private String motifRefus;
    private ClientSummaryDto client;
}
