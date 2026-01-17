package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rendez_vous_proposition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousProposition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proposition")
    private Integer idProposition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin")
    private Administrateur administrateur;

    @Column(name = "date_debut", nullable = false)
    private Instant dateDebut;

    @Column(name = "date_fin", nullable = false)
    private Instant dateFin;

    @Column(name = "statut", length = 20, nullable = false)
    private String statut;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
