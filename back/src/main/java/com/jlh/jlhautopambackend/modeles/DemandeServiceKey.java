package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DemandeServiceKey implements Serializable {
    @Column(name="id_demande")
    private Integer idDemande;

    @Column(name="id_service")
    private Integer idService;
}