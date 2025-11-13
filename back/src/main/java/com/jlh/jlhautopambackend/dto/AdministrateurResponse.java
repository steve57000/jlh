package com.jlh.jlhautopambackend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdministrateurResponse {
    private Integer idAdmin;
    private String email;
    private String nom;
    private String prenom;
    // Si vous voulez renvoyer les disponibilités, n’incluez que leur ID :
    private List<DisponibiliteIdDto> disponibilites;
}
