package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ClientStatsDto {
    private long enAttente;
    private long traitees;
    private long annulees;
    private long rdvAvenir;
    private long demandesLibres;
    private long demandesService;
    private long demandesDevis;
    private long rdvLies;
    private long rdvNonLies;
}
