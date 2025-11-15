package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "Creneau")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Creneau {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCreneau;

    @Column(nullable = false)
    private Instant dateDebut;

    @Column(nullable = false)
    private Instant dateFin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "code_statut")
    private StatutCreneau statut;

    @OneToMany(mappedBy = "creneau", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Disponibilite> disponibilites;
}
