package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.TypeDemandeDto;
import com.jlh.jlhautopambackend.modeles.TypeDemande;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TypeDemandeMapperTest {

    private TypeDemandeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TypeDemandeMapper();
    }

    @Test
    @DisplayName("toDto should map entity to DTO")
    void toDto_mapsEntityToDto() {
        TypeDemande ent = TypeDemande.builder()
                .codeType("T1")
                .libelle("Entretien")
                .build();

        TypeDemandeDto dto = mapper.toDto(ent);


        assertThat(dto).isNotNull();
        assertThat(dto.getCodeType()).isEqualTo("T1");
        assertThat(dto.getLibelle()).isEqualTo("Entretien");
    }

    @Test
    void toEntity_mapsDtoToEntity() {
        TypeDemandeDto dto = TypeDemandeDto.builder()
                .codeType("T2")
                .libelle("Réparation")
                .build();

        TypeDemande ent = mapper.toEntity(dto);

        assertNotNull(ent);
        assertEquals("T2", ent.getCodeType());
        assertEquals("Réparation", ent.getLibelle());
    }

}
