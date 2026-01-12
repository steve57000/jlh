package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idClient;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Column(name="email_verified", nullable=false)
    private boolean emailVerified = false;

    @Column(name="email_verified_at")
    private Instant emailVerifiedAt;

    @Column(nullable = false)
    private String immatriculation;

    @Column(name = "vehicule_marque", length = 100)
    private String vehiculeMarque;

    @Column(name = "vehicule_modele", length = 100)
    private String vehiculeModele;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicule_energie", length = 30)
    private EnergieVehicule vehiculeEnergie;

    @Column(length = 20, nullable = false)
    private String telephone;

    // ✅ On éclate l’adresse
    @Column(name="adresse_ligne1", length = 255)
    private String adresseLigne1;

    @Column(name="adresse_ligne2", length = 255)
    private String adresseLigne2;

    @Column(name="adresse_code_postal", length = 20)
    private String adresseCodePostal;

    @Column(name="adresse_ville", length = 100)
    private String adresseVille;
}
