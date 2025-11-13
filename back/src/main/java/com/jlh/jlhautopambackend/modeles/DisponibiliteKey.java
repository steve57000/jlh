package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DisponibiliteKey implements Serializable {
    @Column(name="id_admin")
    private Integer idAdmin;

    @Column(name="id_creneau")
    private Integer idCreneau;
}