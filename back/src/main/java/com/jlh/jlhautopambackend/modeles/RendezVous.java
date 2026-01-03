package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rendez_vous")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RendezVous {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRdv;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_demande", unique = true, nullable = false)
    private Demande demande;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_admin", nullable = false)
    private Administrateur administrateur;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_creneau", unique = true, nullable = false)
    private Creneau creneau;

    @ManyToOne(optional = false)
    @JoinColumn(name = "code_statut", nullable = false)
    private StatutRendezVous statut;

    @Column(columnDefinition = "TEXT")
    private String commentaire;
}
