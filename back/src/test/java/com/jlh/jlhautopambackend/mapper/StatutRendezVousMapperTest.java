package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.StatutRendezVousDto;
import com.jlh.jlhautopambackend.modeles.StatutRendezVous;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StatutRendezVousMapperTest {

    private StatutRendezVousMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StatutRendezVousMapper();
    }

    @Test
    @DisplayName("toDto should map entity to DTO")
    void testToDto() {
        StatutRendezVous entity = StatutRendezVous.builder()
                .codeStatut("RV_OK")
                .libelle("Rendez-vous confirmé")
                .build();

        StatutRendezVousDto dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodeStatut()).isEqualTo("RV_OK");
        assertThat(dto.getLibelle()).isEqualTo("Rendez-vous confirmé");
    }

    @Test
    @DisplayName("toEntity should map DTO to entity")
    void testToEntity() {
        StatutRendezVousDto dto = StatutRendezVousDto.builder()
                .codeStatut("RV_KO")
                .libelle("Rendez-vous annulé")
                .build();

        StatutRendezVous entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getCodeStatut()).isEqualTo("RV_KO");
        assertThat(entity.getLibelle()).isEqualTo("Rendez-vous annulé");
    }

    @Test
    @DisplayName("updateEntity should update only the libelle")
    void testUpdateEntity() {
        // Entité initiale
        StatutRendezVous entity = StatutRendezVous.builder()
                .codeStatut("X")
                .libelle("Ancien libellé")
                .build();
        // DTO avec nouveau libellé
        StatutRendezVousDto dto = StatutRendezVousDto.builder()
                .codeStatut("X")          // codeStatut n’est pas modifié par updateEntity
                .libelle("Nouveau texte")
                .build();

        mapper.updateEntity(dto, entity);

        // On vérifie que seul le libellé a changé
        assertThat(entity.getCodeStatut()).isEqualTo("X");
        assertThat(entity.getLibelle()).isEqualTo("Nouveau texte");
    }
}
