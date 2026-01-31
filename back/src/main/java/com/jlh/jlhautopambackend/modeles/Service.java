package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer idService;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_longue", columnDefinition = "TEXT")
    private String descriptionLongue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_icon")
    private ServiceIcon icon;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantite_mode", nullable = false, length = 20)
    private ServiceQuantiteMode quantiteMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "prix_mode", nullable = false, length = 20)
    private ServicePrixMode prixMode;

    @Column(name = "taille_lot")
    private Integer tailleLot;

    @Column(name = "quantite_max", nullable = false)
    private Integer quantiteMax;

    @Column(nullable = false)
    private boolean archived = false;
}
