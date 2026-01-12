package com.jlh.jlhautopambackend.dto;

import com.jlh.jlhautopambackend.modeles.EnergieVehicule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientMeUpdateRequest {

    // Téléphone FR : 0X XX XX XX XX (sans espaces côté backend)
    @Pattern(regexp = "^0[1-9](\\d{2}){4}$", message = "Format téléphone FR invalide (0X XX XX XX XX)")
    private String telephone;

    // Immatriculation SIV stricte : AA-123-AA
    @Pattern(regexp = "^[A-Z]{2}-\\d{3}-[A-Z]{2}$", message = "Immatriculation invalide (format AA-123-AA)")
    private String immatriculation;

    @Size(max = 100, message = "Marque du véhicule trop longue")
    private String vehiculeMarque;

    @Size(max = 100, message = "Modèle du véhicule trop long")
    private String vehiculeModele;

    private EnergieVehicule vehiculeEnergie;

    @Valid
    private Adresse adresse;

    @Data
    public static class Adresse {
        @Size(max = 255, message = "Adresse ligne 1 trop longue")
        private String ligne1;

        @Size(max = 255, message = "Adresse ligne 2 trop longue")
        private String ligne2;

        // Code postal strict : 5 chiffres
        @Pattern(regexp = "^\\d{5}$", message = "Code postal invalide (5 chiffres)")
        private String codePostal;

        @Size(max = 100, message = "Ville trop longue")
        private String ville;
    }
}
