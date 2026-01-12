package com.jlh.jlhautopambackend.dto;

import com.jlh.jlhautopambackend.modeles.EnergieVehicule;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeRequest {
    private Instant dateDemande;
    private Integer clientId;
    /** Code du type de demande (clé primaire de TypeDemande) */
    private String codeType;
    /** Code du statut de la demande (clé primaire de StatutDemande) */
    private String codeStatut;
    private String immatriculation;
    private String vehiculeMarque;
    private String vehiculeModele;
    private EnergieVehicule vehiculeEnergie;
    private String telephone;
    private String adresseLigne1;
    private String adresseLigne2;
    private String adresseCodePostal;
    private String adresseVille;
    private List<DemandeServiceDto> services;
}
