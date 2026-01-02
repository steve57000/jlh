package com.jlh.jlhautopambackend.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousTimelineDto {
    private Integer idRdv;
    private String codeStatut;
    private String libelleStatut;
    private Instant dateDebut;
    private Instant dateFin;
}
