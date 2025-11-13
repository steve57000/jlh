// src/main/java/com/jlh/jlhautopambackend/dto/ClientMeDto.java
package com.jlh.jlhautopambackend.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ClientMeDto {
    private Integer idClient;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String immatriculation;
    private AddressDto adresse;

    @Data @Builder
    public static class AddressDto {
        private String ligne1;
        private String ligne2;
        private String codePostal;
        private String ville;
    }
}
