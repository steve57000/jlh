package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ClientVehicle vehicule;

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

    public String getImmatriculation() {
        return vehicule != null ? vehicule.getImmatriculation() : null;
    }

    public void setImmatriculation(String immatriculation) {
        if (immatriculation == null && vehicule == null) {
            return;
        }
        ensureVehicle().setImmatriculation(immatriculation);
    }

    public String getVehiculeMarque() {
        return vehicule != null ? vehicule.getVehiculeMarque() : null;
    }

    public void setVehiculeMarque(String vehiculeMarque) {
        if (vehiculeMarque == null && vehicule == null) {
            return;
        }
        ensureVehicle().setVehiculeMarque(vehiculeMarque);
    }

    public String getVehiculeModele() {
        return vehicule != null ? vehicule.getVehiculeModele() : null;
    }

    public void setVehiculeModele(String vehiculeModele) {
        if (vehiculeModele == null && vehicule == null) {
            return;
        }
        ensureVehicle().setVehiculeModele(vehiculeModele);
    }

    public EnergieVehicule getVehiculeEnergie() {
        return vehicule != null ? vehicule.getVehiculeEnergie() : null;
    }

    public void setVehiculeEnergie(EnergieVehicule vehiculeEnergie) {
        if (vehiculeEnergie == null && vehicule == null) {
            return;
        }
        ensureVehicle().setVehiculeEnergie(vehiculeEnergie);
    }

    public void setVehicule(ClientVehicle vehicule) {
        this.vehicule = vehicule;
        if (vehicule != null && vehicule.getClient() != this) {
            vehicule.setClient(this);
        }
    }

    private ClientVehicle ensureVehicle() {
        if (vehicule == null) {
            vehicule = ClientVehicle.builder()
                    .client(this)
                    .build();
        } else if (vehicule.getClient() == null) {
            vehicule.setClient(this);
        }
        return vehicule;
    }
}
