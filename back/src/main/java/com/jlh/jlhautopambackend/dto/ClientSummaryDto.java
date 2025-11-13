package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ClientSummaryDto {
    private Integer idClient;
    private String nom;
    private String prenom;
    private String email;
    private String immatriculation;
    private String telephone;
    private String adresseLigne1;
    private String adresseLigne2;
    private String adresseCodePostal;
    private String adresseVille;
}
