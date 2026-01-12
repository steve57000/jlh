package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client_vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientVehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVehicle;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_client", nullable = false, unique = true)
    private Client client;

    @Column(nullable = false)
    private String immatriculation;

    @Column(name = "vehicule_marque", length = 100)
    private String vehiculeMarque;

    @Column(name = "vehicule_modele", length = 100)
    private String vehiculeModele;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicule_energie", length = 30)
    private EnergieVehicule vehiculeEnergie;
}
