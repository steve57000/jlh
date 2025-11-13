package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdministrateurMapperTest {

    private final AdministrateurMapper mapper = Mappers.getMapper(AdministrateurMapper.class);

    @Test
    void toEntity_ignoreIdAndDisponibilites() {
        AdministrateurRequest req = AdministrateurRequest.builder()
                .email("user1")
                .motDePasse("pwd")
                .nom("Dupont")
                .prenom("Jean")
                .build();

        Administrateur ent = mapper.toEntity(req);
        assertThat(ent.getEmail()).isEqualTo("user1");
        assertThat(ent.getMotDePasse()).isEqualTo("pwd");
        assertThat(ent.getNom()).isEqualTo("Dupont");
        assertThat(ent.getPrenom()).isEqualTo("Jean");
        assertThat(ent.getIdAdmin()).isNull();               // ignoré à la création
        assertThat(ent.getDisponibilites()).isNull();        // ignoré :contentReference[oaicite:4]{index=4}
    }

    @Test
    void toResponse_mapsDisponibilitesToDto() {
        Disponibilite d1 = Disponibilite.builder()
                .id(new DisponibiliteKey(5, 42))
                .build();
        Administrateur ent = Administrateur.builder()
                .idAdmin(5)
                .email("u")
                .nom("X")
                .prenom("Y")
                .disponibilites(List.of(d1))
                .build();

        AdministrateurResponse resp = mapper.toResponse(ent);
        assertThat(resp.getIdAdmin()).isEqualTo(5);
        assertThat(resp.getEmail()).isEqualTo("u");
        assertThat(resp.getNom()).isEqualTo("X");
        // vérifie le mapping de la liste des IDs
        assertThat(resp.getDisponibilites())
                .containsExactly(new DisponibiliteIdDto(5, 42));
    }
}
