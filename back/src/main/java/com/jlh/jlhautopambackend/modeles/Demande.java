package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "demande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Demande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    @EqualsAndHashCode.Include
    private Integer idDemande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @Column(name = "date_demande", nullable = false)
    private Instant dateDemande;

    @ManyToOne
    @JoinColumn(name = "code_type", referencedColumnName = "code_type", nullable = false)
    private TypeDemande typeDemande;

    @ManyToOne
    @JoinColumn(name = "code_statut", referencedColumnName = "code_statut", nullable = false)
    private StatutDemande statutDemande;

    // Si RDV :
    @OneToOne(mappedBy = "demande") // ou @OneToOne @JoinColumn(name="id_demande") selon ton modèle
    private RendezVous rendezVous;

    @OneToMany(mappedBy = "demande") // via DemandeService.id.idDemande
    private Set<DemandeService> services;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("creeLe ASC")
    @Builder.Default
    private Set<DemandeDocument> documents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private Set<DemandeTimeline> timelineEntries = new LinkedHashSet<>();
}
