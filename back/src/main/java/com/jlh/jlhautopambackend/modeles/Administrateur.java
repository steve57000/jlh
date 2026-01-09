package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "administrateur")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Administrateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAdmin;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String motDePasse;

    private String nom;
    private String prenom;

    @Column(unique=true)
    private String email;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ADMIN'")
    @Column(name = "niveau_acces", nullable = false, length = 30)
    private NiveauAccesAdministrateur niveauAcces;

    @OneToMany(mappedBy = "administrateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Disponibilite> disponibilites;
}
