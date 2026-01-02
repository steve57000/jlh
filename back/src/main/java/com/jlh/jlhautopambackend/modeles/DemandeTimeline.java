package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "demande_timeline")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_timeline")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Demande demande;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_evenement", length = 30, nullable = false)
    private DemandeTimelineType type;

    @Column(name = "cree_le", nullable = false)
    private Instant createdAt;

    @Column(name = "cree_par", length = 150)
    private String createdBy;

    @Column(name = "cree_par_role", length = 30)
    private String createdByRole;

    @Column(name = "visible_client", nullable = false)
    private boolean visibleClient;

    @Column(name = "statut_code", length = 20)
    private String statutCode;

    @Column(name = "statut_libelle", length = 100)
    private String statutLibelle;

    @Column(name = "commentaire", length = 1000)
    private String commentaire;

    @Column(name = "montant_valide", precision = 12, scale = 2)
    private BigDecimal montantValide;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "document_nom", length = 255)
    private String documentNom;

    @Column(name = "document_url", length = 512)
    private String documentUrl;

    @Column(name = "rendezvous_id")
    private Integer rendezVousId;

    @Column(name = "rendezvous_statut_code", length = 20)
    private String rendezVousStatutCode;

    @Column(name = "rendezvous_statut_libelle", length = 100)
    private String rendezVousStatutLibelle;

    @Column(name = "rendezvous_date_debut")
    private Instant rendezVousDateDebut;

    @Column(name = "rendezvous_date_fin")
    private Instant rendezVousDateFin;
}
