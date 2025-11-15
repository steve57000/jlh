package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Demande_Service")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DemandeService {
    @EmbeddedId
    private DemandeServiceKey id;

    @ManyToOne
    @MapsId("idDemande")
    @JoinColumn(name = "id_demande")
    private Demande demande;

    @ManyToOne
    @MapsId("idService")
    @JoinColumn(name = "id_service")
    private Service service;

    @Column(nullable = false)
    private Integer quantite;
}

