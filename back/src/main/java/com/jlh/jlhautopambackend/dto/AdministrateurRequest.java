package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdministrateurRequest {
    private String email;
    private String motDePasse;
    private String nom;
    private String prenom;

}
