package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "Statut_Creneau")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StatutCreneau {
    @Id
    @Column(name="code_statut", length=20)
    private String codeStatut;

    @Column(nullable=false, length=100)
    private String libelle;

    // relation inverse (optionnel)
    @OneToMany(mappedBy = "statut")
    private List<Creneau> creneaux;
}