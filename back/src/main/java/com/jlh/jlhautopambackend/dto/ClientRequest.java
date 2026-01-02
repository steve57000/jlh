// src/main/java/com/jlh/jlhautopambackend/dto/ClientRequest.java
package com.jlh.jlhautopambackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit faire au moins 6 caractères")
    private String motDePasse;

    @NotBlank(message = "L'immatriculation est obligatoire")
    private String immatriculation;

    @NotBlank(message = "La marque du véhicule est obligatoire")
    @Size(max = 100, message = "La marque du véhicule est trop longue")
    private String vehiculeMarque;

    @NotBlank(message = "Le modèle du véhicule est obligatoire")
    @Size(max = 100, message = "Le modèle du véhicule est trop long")
    private String vehiculeModele;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresseLigne1;

    private String adresseLigne2;

    @NotBlank(message = "Le code postal est obligatoire")
    private String codePostal;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;
}
