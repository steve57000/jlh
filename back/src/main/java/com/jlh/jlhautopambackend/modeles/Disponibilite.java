package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Disponibilite")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Disponibilite {
    @EmbeddedId
    private DisponibiliteKey id;

    @ManyToOne
    @MapsId("idAdmin")
    @JoinColumn(name = "id_admin")
    private Administrateur administrateur;

    @ManyToOne
    @MapsId("idCreneau")
    @JoinColumn(name = "id_creneau")
    private Creneau creneau;
}
