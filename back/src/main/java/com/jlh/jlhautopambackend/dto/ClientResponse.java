package com.jlh.jlhautopambackend.dto;

import com.jlh.jlhautopambackend.modeles.EnergieVehicule;
import lombok.*;

import java.time.Instant;

// src/main/java/com/jlh/jlhautopambackend/dto/ClientResponse.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {
    private Integer idClient;
    private String nom;
    private String prenom;
    private String email;
    private String immatriculation;
    private String vehiculeMarque;
    private String vehiculeModele;
    private EnergieVehicule vehiculeEnergie;
    private String telephone;

    // on garde la forme éclatée
    private String adresseLigne1;
    private String adresseLigne2;
    private String codePostal;
    private String ville;

    private boolean emailVerified;
    private Instant emailVerifiedAt;
}
