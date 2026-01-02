package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "demande_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document")
    private Long idDocument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Demande demande;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "url_private", length = 512)
    private String urlPrivate;

    @Column(name = "type_contenu", length = 100)
    private String typeContenu;

    @Column(name = "taille_octets")
    private Long tailleOctets;

    @Column(name = "visible_client", nullable = false)
    private boolean visibleClient;

    @Column(name = "cree_par", length = 150)
    private String creePar;

    @Column(name = "cree_par_role", length = 30)
    private String creeParRole;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;
}
