package com.jlh.jlhautopambackend.payload;

import com.jlh.jlhautopambackend.modeles.EnergieVehicule;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @NotBlank @Email @Size(max = 150) String email,

        // Mot de passe: min 8, 1 maj, 1 min, 1 chiffre (même règle que le front)
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Doit contenir au moins 8 caractères dont 1 majuscule, 1 minuscule et 1 chiffre"
        )
        String motDePasse,

        // Téléphone FR simple: 0X XX XX XX XX
        @NotBlank
        @Pattern(regexp = "^0[1-9](\\d{2}){4}$", message = "Format téléphone FR invalide")
        String telephone,

        // Immatriculation SIV: AA-123-AA
        @NotBlank
        @Pattern(regexp = "^[A-Z]{2}-\\d{3}-[A-Z]{2}$", message = "Format immatriculation invalide (AA-123-AA)")
        String immatriculation,

        @NotBlank
        @Size(max = 100, message = "Marque de véhicule trop longue")
        String vehiculeMarque,

        @NotBlank
        @Size(max = 100, message = "Modèle de véhicule trop long")
        String vehiculeModele,

        EnergieVehicule vehiculeEnergie,

        // ✅ Adresse éclatée
        @NotBlank String adresseLigne1,
        String adresseLigne2,
        @NotBlank String codePostal,
        @NotBlank String ville,

        @NotNull Boolean consentRgpd
) {}
