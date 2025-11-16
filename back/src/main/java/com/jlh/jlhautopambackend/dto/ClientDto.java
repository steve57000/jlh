package com.jlh.jlhautopambackend.dto;

import lombok.Data;

@Data
public class ClientDto {
    private Integer idClient;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
}
