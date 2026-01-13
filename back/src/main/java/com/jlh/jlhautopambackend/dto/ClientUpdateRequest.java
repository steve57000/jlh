package com.jlh.jlhautopambackend.dto;

import com.jlh.jlhautopambackend.modeles.EnergieVehicule;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientUpdateRequest {
    private String nom;
    private String prenom;

    @Email(message = "Email invalide")
    private String email;

    @Size(min = 6, message = "Le mot de passe doit faire au moins 6 caractères")
    private String motDePasse;

    private String immatriculation;
    private String vehiculeMarque;
    private String vehiculeModele;
    private EnergieVehicule vehiculeEnergie;
    private String telephone;
    private String adresseLigne1;
    private String adresseLigne2;
    private String codePostal;
    private String ville;
}
