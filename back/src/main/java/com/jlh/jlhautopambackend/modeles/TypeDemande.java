package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "Type_Demande")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TypeDemande {
    @Id
    @Column(name="code_type", length=20)
    private String codeType;

    @Column(nullable=false, length=100)
    private String libelle;
}