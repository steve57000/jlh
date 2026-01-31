package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "avis_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avis")
    private Long idAvis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @Column(name = "note", nullable = false)
    private Integer note;

    @Column(name = "commentaire", length = 1000)
    private String commentaire;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private AvisServiceStatut statut;

    @Column(name = "motif_refus", length = 1000)
    private String motifRefus;
}
