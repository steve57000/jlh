package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(name = "devis")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Devis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDevis;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_demande", unique = true, nullable = false)
    private Demande demande;

    @Column(nullable = false)
    private Instant dateDevis;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTotal;
}
