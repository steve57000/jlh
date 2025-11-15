package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Statut_Demande")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StatutDemande {
    @Id
    @Column(name = "code_statut", length = 20)
    private String codeStatut;

    @Column(nullable = false, length = 100)
    private String libelle;
}
