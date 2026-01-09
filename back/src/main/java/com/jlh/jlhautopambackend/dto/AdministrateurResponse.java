package com.jlh.jlhautopambackend.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdministrateurResponse {
    private Integer idAdmin;
    private String email;
    private String username;
    private String nom;
    private String prenom;
    private String niveauAcces;
    // Si vous voulez renvoyer les disponibilités, n’incluez que leur ID :
    private List<DisponibiliteIdDto> disponibilites;

    @JsonGetter("username")
    public String getUsername() {
        return username != null && !username.isBlank() ? username : email;
    }
}
