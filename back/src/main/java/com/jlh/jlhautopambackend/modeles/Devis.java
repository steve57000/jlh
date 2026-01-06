package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(name = "devis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDevis;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_demande", unique = true, nullable = false)
    private Demande demande;

    @Column(nullable = false)
    private Instant dateDevis;

    /** Montant global du devis (main d’œuvre + pièces) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTotal;

    /** Montant spécifique de la main d’œuvre (optionnel) */
    @Column(name = "montant_main_oeuvre", precision = 12, scale = 2)
    private BigDecimal montantMainOeuvre;

    /** Montant des pièces (optionnel, somme des lignes de services) */
    @Column(name = "montant_pieces", precision = 12, scale = 2)
    private BigDecimal montantPieces;

    @Column(name = "id_rdv")
    private Integer rendezVousId;
}
