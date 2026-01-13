package com.jlh.jlhautopambackend.dto;

import com.jlh.jlhautopambackend.modeles.EnergieVehicule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeClientUpdateRequest {
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
