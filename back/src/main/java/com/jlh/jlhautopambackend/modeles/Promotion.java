package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "promotion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Promotion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPromotion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_admin", nullable = false)
    private Administrateur administrateur;

    // URL ou chemin vers l’image (vous pouvez aussi stocker le BLOB si besoin)
    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Instant validFrom;

    @Column(nullable = false)
    private Instant validTo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
}
