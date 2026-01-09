package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rendez_vous")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RendezVous {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRdv;

    @OneToOne(optional = true)
    @JoinColumn(name = "id_demande", unique = true)
    private Demande demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "id_demande_service", referencedColumnName = "id_demande"),
            @JoinColumn(name = "id_service", referencedColumnName = "id_service")
    })
    private DemandeService demandeService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_devis")
    private Devis devis;

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

    @Transient
    public Client getClient() {
        return demande != null ? demande.getClient() : null;
    }
}
