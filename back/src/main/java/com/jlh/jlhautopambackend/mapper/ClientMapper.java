// src/main/java/com/jlh/jlhautopambackend/mapper/ClientMapper.java
package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.ClientDto;
import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.modeles.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class ClientMapper {

    protected PasswordEncoder passwordEncoder;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Mapping(target = "idClient", ignore = true)
    @Mapping(target = "motDePasse", expression = "java(passwordEncoder.encode(dto.getMotDePasse()))")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "emailVerifiedAt", ignore = true)
    // champs adresse
    @Mapping(target = "adresseLigne1", source = "adresseLigne1")
    @Mapping(target = "adresseLigne2", source = "adresseLigne2")
    @Mapping(target = "adresseCodePostal", source = "codePostal")
    @Mapping(target = "adresseVille", source = "ville")
    @Mapping(target = "vehiculeMarque", source = "vehiculeMarque")
    @Mapping(target = "vehiculeModele", source = "vehiculeModele")
    public abstract Client toEntity(ClientRequest dto);

    @Mapping(target = "codePostal", source = "adresseCodePostal")
    @Mapping(target = "ville", source = "adresseVille")
    @Mapping(target = "vehiculeMarque", source = "vehiculeMarque")
    @Mapping(target = "vehiculeModele", source = "vehiculeModele")
    public abstract ClientResponse toResponse(Client entity);

    @Mapping(target = "adresse", expression = "java(formatAdresse(entity))")
    public abstract ClientDto toDto(Client entity);

    protected String formatAdresse(Client entity) {
        if (entity == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (entity.getAdresseLigne1() != null && !entity.getAdresseLigne1().isBlank()) {
            sb.append(entity.getAdresseLigne1().trim());
        }
        if (entity.getAdresseLigne2() != null && !entity.getAdresseLigne2().isBlank()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(entity.getAdresseLigne2().trim());
        }
        if (entity.getAdresseCodePostal() != null && !entity.getAdresseCodePostal().isBlank()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(entity.getAdresseCodePostal().trim());
        }
        if (entity.getAdresseVille() != null && !entity.getAdresseVille().isBlank()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(entity.getAdresseVille().trim());
        }
        return sb.length() == 0 ? null : sb.toString();
    }
}
