package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Administrateur")
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

    @OneToMany(mappedBy = "administrateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Disponibilite> disponibilites;
}
