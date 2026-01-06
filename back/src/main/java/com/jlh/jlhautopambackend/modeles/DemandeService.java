package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import java.math.BigDecimal;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "demande_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DemandeService {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private DemandeServiceKey id = new DemandeServiceKey();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idDemande")
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idService")
    @JoinColumn(name = "id_service", nullable = false)
    private Service service;

    @Column(name = "quantite", nullable = false)
    @Min(1)
    private Integer quantite;

    @Column(name = "libelle_service", nullable = false, length = 100)
    private String libelleService;

    @Column(name = "description_service", columnDefinition = "TEXT")
    private String descriptionService;

    @Column(name = "prix_unitaire_service", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaireService;

    @Column(name = "id_rdv")
    private Integer rendezVousId;

    public void snapshotFromService(Service source) {
        if (source == null) {
            return;
        }
        this.libelleService = source.getLibelle();
        this.descriptionService = source.getDescription();
        this.prixUnitaireService = source.getPrixUnitaire();
    }
}
