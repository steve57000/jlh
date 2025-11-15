package com.jlh.jlhautopambackend.modeles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "Demande")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Demande {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDemande;

    @Column(nullable = false)
    private Instant dateSoumission;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "code_type", nullable = false)
    private TypeDemande typeDemande;

    @ManyToOne(optional = false)
    @JoinColumn(name = "code_statut", nullable = false)
    private StatutDemande statutDemande;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DemandeService> services;
}
