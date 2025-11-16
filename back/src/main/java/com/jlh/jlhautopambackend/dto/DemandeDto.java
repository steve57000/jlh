package com.jlh.jlhautopambackend.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class DemandeDto {
    private Integer idDemande;
    private Instant dateSoumission;
    private String codeType;
    private String typeLibelle;
    private String codeStatut;
    private String statutLibelle;
    private ClientDto client;
    private List<DemandeServiceDto> services;
}
