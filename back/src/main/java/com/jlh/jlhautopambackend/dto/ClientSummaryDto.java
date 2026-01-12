package com.jlh.jlhautopambackend.dto;

import com.jlh.jlhautopambackend.modeles.EnergieVehicule;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ClientSummaryDto {
    private Integer idClient;
    private String nom;
    private String prenom;
    private String email;
    private String immatriculation;
    private String vehiculeMarque;
    private String vehiculeModele;
    private EnergieVehicule vehiculeEnergie;
    private String telephone;
    private String adresseLigne1;
    private String adresseLigne2;
    private String adresseCodePostal;
    private String adresseVille;
}
